/*
 * Copyright (c) 2022 Gino Atlas.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package technology.iatlas.spaceup.services

import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.config.SpaceUpSftpConfig
import technology.iatlas.spaceup.config.SpaceUpSshConfig
import technology.iatlas.spaceup.core.annotations.Installed
import technology.iatlas.spaceup.core.cmd.CommandInf
import technology.iatlas.spaceup.core.cmd.SshResponse
import technology.iatlas.spaceup.core.helper.colored
import technology.iatlas.spaceup.dto.db.Ssh
import java.io.ByteArrayOutputStream
import java.io.File

@Installed
@Context
class SshService(
    private val sshConfig: SpaceUpSshConfig,
    private val sftpConfig: SpaceUpSftpConfig,
    private val dbService: DbService,
    private val spaceUpService: SpaceUpService,
    private val securityService: SecurityService
    ) {
    private val log = LoggerFactory.getLogger(SshService::class.java)

    private lateinit var session: Session

    @Value("\${spaceup.dev.ssh.db-credentials}")
    private lateinit var useDbCredentials: String

    // Configure SSH
    fun initSSH() {
        val jsch = JSch()

        var username = ""
        var password = ""
        var host = ""

        if(spaceUpService.isDevMode() && useDbCredentials == "false") {
            colored {
                log.debug("Use configuration from parameters in dev mode.".yellow)
            }
            username = sshConfig.username!!
            password = sshConfig.password!!
            host = sshConfig.host!!
        } else {
            log.debug("Take saved credentials")
            val db = dbService.getDb()
            val sshRepo = db.getCollection<Ssh>()
            log.debug("Assuming there is only one configuration")
            val ssh = sshRepo.findOne()!!

            securityService.decrypt(ssh) {
                username = ssh.username
                password = ssh.password
                host = ssh.server
            }
        }

        if((sshConfig.privatekey == null || sshConfig.privatekey!!.isEmpty())) {
            colored {
                if(log.isDebugEnabled) {
                    // Show this only if debug is enabled
                    log.warn("To authenticate with Privatekey supply" +
                            " '-spaceup.ssh.privatekey=\"your path to key\"' ".yellow + "to JAR.")
                }
            }
        }

        if(sshConfig.port == null) {
            log.error("Provide SSH port '-spaceup.ssh.port=22' to JAR.")
        }

        val privatekey: String? = sshConfig.privatekey
        if (!privatekey.isNullOrEmpty()) {
            jsch.addIdentity(File(privatekey).normalize().path)
            session = jsch.getSession(username, host, Integer.valueOf(sshConfig.port!!))

            log.info("Authenticate SSH via private key!")
        } else {
            session = jsch.getSession(
                    username, host, Integer.valueOf(sshConfig.port!!))
            session.setPassword(password)

            log.info("Authenticate SSH via password!")
        }
        session.setConfig("StrictHostKeyChecking", "no")
        try {
            session.connect()
        } catch (jschException: JSchException) {
            log.error(jschException.message)
        }
    }

    suspend fun execute(command: CommandInf): SshResponse {
        log.trace("Execute $command")
        if(!this::session.isInitialized || !session.isConnected) {
            initSSH()
        }

        val channel: ChannelExec = try {
            session.openChannel("exec") as ChannelExec
        } catch (shhEx: JSchException) {
            log.error("SSH Session is down. Will try to reconnect.")
            initSSH()
            session.openChannel("exec") as ChannelExec
        }

        try {
            channel.setCommand(command.parameters.joinToString(" "))
            val responseStream = ByteArrayOutputStream()
            val errorResponseStream = ByteArrayOutputStream()
            channel.outputStream = responseStream
            channel.setErrStream(errorResponseStream)

            try {
                channel.connect()
            } catch (shhEx: JSchException) {
                log.error("SSH Session is down. Will try to reconnect.")
                initSSH()
            }

            // When then channel close itself, we retrieved the data
            while (channel.isConnected) {
                delay(50)
            }

            val stdout = String(responseStream.toByteArray())
            var stderr = String(errorResponseStream.toByteArray())
            if(stderr.isNotEmpty() && channel.exitStatus != 0) {
                stderr = """
                    Command: ${command.parameters.joinToString { " " }}
                    terminated with exit code: ${channel.exitStatus}
                """.trimIndent()
                colored {
                    log.error(stderr.red)
                }
            }
            val sshResponse = SshResponse(stdout, stderr)
            log.trace(sshResponse.toString())

            return sshResponse
        } finally {
            //session.disconnect()
            channel.disconnect()
        }
    }

    /**
     * Upload a shell script via SFTP and execute it optionally
     *
     */
    suspend fun upload(cmd: CommandInf): SshResponse {
        log.debug("Upload $cmd")
        if(!session.isConnected) {
            initSSH()
        }

        val db = dbService.getDb()
        val sshRepo = db.getCollection<Ssh>()
        val ssh = sshRepo.find().first()!!

        val file = cmd.shellScript
        val remotefile =
            sftpConfig.remotedir?.replace("~", "/home/${ssh.username}") + "/${file.name}"

        val sftpChannel: Channel = session.openChannel("sftp") as Channel

        val executionChannel: ChannelExec = session.openChannel("exec") as ChannelExec
        val executeCmd = cmd.parameters.joinToString(" ")
        executionChannel.setCommand(executeCmd)

        val responseExecution = ByteArrayOutputStream()
        val errorExecution = ByteArrayOutputStream()
        executionChannel.outputStream = responseExecution
        executionChannel.setErrStream(errorExecution)

        var sshResponse = SshResponse("", "")
        try {
            sftpChannel.connect()
            val sftp = sftpChannel as ChannelSftp
            log.debug("Upload script ${file.name} to $remotefile")
            withContext(Dispatchers.IO) {
                val os = System.getProperty("os.name")

                if(os.lowercase().contains("windows")) {
                    val localScript = file.scriptPath?.file ?: ""
                    sftp.put(File(localScript).canonicalPath, remotefile, ChannelSftp.OVERWRITE)
                } else {
                    sftp.put(file.scriptPath?.openStream(), remotefile, ChannelSftp.OVERWRITE)
                }
            }

            if(file.execute) {
                log.trace("""
                    "Execute script: ${cmd.parameters}
                    $executeCmd
                """.trimIndent())
                executionChannel.connect()

                // When then channel close itself, we retrieved the data
                while (executionChannel.isConnected) {
                    delay(50)
                }

                val stdout = String(responseExecution.toByteArray())
                var stderr = String(errorExecution.toByteArray())
                if(stderr.isNotEmpty() && executionChannel.exitStatus != 0) {
                    stderr = """
                    Script: ${cmd.shellScript}
                    Command: ${cmd.parameters.joinToString { " " }}
                    terminated with exit code: ${executionChannel.exitStatus}
                """.trimIndent()
                    colored {
                        log.error(stderr.red)
                    }
                }
                sshResponse = SshResponse(stdout, stderr)
                // SshResponse
                log.trace(sshResponse.toString())
            }

            log.trace(sshResponse.toString())
            return sshResponse
        } finally {
            sftpChannel.disconnect()
            executionChannel.disconnect()
        }
    }
}