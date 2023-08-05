/*
 * Copyright (c) 2022-2023 Thraax Session <spaceup@iatlas.technology>.
 *
 * SpaceUp-Server is free software; You can redistribute it and/or modify it under the terms of:
 *   - the GNU Affero General Public License version 3 as published by the Free Software Foundation.
 * You don't have to do anything special to accept the license and you don’t have to notify anyone which that you have made that decision.
 *
 * SpaceUp-Server is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See your chosen license for more details.
 *
 * You should have received a copy of both licenses along with SpaceUp-Server
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * There is a strong belief within us that the license we have chosen provides not only the best solution for providing you with the essential freedom necessary to use SpaceUp-Server within your projects, but also for maintaining enough copyleft strength for us to feel confident and secure with releasing our hard work to the public. For your convenience we've included our own interpretation of the license we chose, which can be seen below.
 *
 * Our interpretation of the GNU Affero General Public License version 3: (Quoted words are words in which there exists a definition within the license to avoid ambiguity.)
 *   1. You must always provide the source code, copyright and license information of SpaceUp-Server whenever you "convey" any part of SpaceUp-Server;
 *      be it a verbatim copy or a modified copy.
 *   2. SpaceUp-Server was developed as a library and has therefore been designed without knowledge of your work; as such the following should be implied:
 *      a) SpaceUp-Server was developed without knowledge of your work; as such the following should be implied:
 *         i)  SpaceUp-Server should not fall under a work which is "based on" your work.
 *         ii) You should be free to use SpaceUp-Server in a work covered by the:
 *             - GNU General Public License version 2
 *             - GNU Lesser General Public License version 2.1
 *             This is due to those licenses classifying SpaceUp-Server as a work which would fall under an "aggregate" work by their terms and definitions;
 *             as such it should not be covered by their terms and conditions. The relevant passages start at:
 *             - Line 129 of the GNU General Public License version 2
 *             - Line 206 of the GNU Lesser General Public License version 2.1
 *      b) If you have not "modified", "adapted" or "extended" SpaceUp-Server then your work should not be bound by this license,
 *         as you are using SpaceUp-Server under the definition of an "aggregate" work.
 *      c) If you have "modified", "adapted" or "extended" SpaceUp-Server then any of those modifications/extensions/adaptations which you have made
 *         should indeed be bound by this license, as you are using SpaceUp-Server under the definition of a "based on" work.
 *
 * Our hopes is that our own interpretation of license aligns perfectly with your own values and goals for using our work freely and securely. If you have any questions at all about the licensing chosen for SpaceUp-Server you can email us directly at spaceup@iatlas.technology or you can get in touch with the license authors (the Free Software Foundation) at licensing@fsf.org to gain their opinion too.
 *
 * Alternatively you can provide feedback and acquire the support you need at our support forum. We'll definitely try and help you as soon as possible, and to the best of our ability; as we understand that user experience is everything, so we want to make you as happy as possible! So feel free to get in touch via our support forum and chat with other users of SpaceUp-Server here at:
 * https://spaceup.iatlas.technology
 *
 * Thanks, and we hope you enjoy using SpaceUp-Server and that it's everything you ever hoped it could be.
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
import io.micronaut.tracing.annotation.ContinueSpan
import io.micronaut.tracing.annotation.NewSpan
import io.micronaut.tracing.annotation.SpanTag
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirst
import org.litote.kmongo.reactivestreams.getCollection
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.config.SpaceUpSshConfig
import technology.iatlas.spaceup.config.SpaceupRemotePathConfig
import technology.iatlas.spaceup.core.annotations.Installed
import technology.iatlas.spaceup.core.cmd.CommandInf
import technology.iatlas.spaceup.core.cmd.SshResponse
import technology.iatlas.spaceup.core.helper.colored
import technology.iatlas.spaceup.dto.Command
import technology.iatlas.spaceup.dto.db.Ssh
import java.io.ByteArrayOutputStream
import java.io.File

@Installed
@Context
open class SshService(
    private val sshConfig: SpaceUpSshConfig,
    private val spaceupRemotePathConfig: SpaceupRemotePathConfig,
    private val dbService: DbService,
    private val spaceUpService: SpaceUpService,
    private val securityService: SecurityService
    ) {
    private val log = LoggerFactory.getLogger(SshService::class.java)

    private lateinit var session: Session

    @Value("\${spaceup.dev.ssh.db-credentials}")
    private var useDbCredentials: Boolean = false

    // Configure SSH
    @ContinueSpan
    suspend fun initSSH() {
        val jsch = JSch()

        var username = ""
        var password = ""
        var host = ""

        if(spaceUpService.isDevMode() && !useDbCredentials) {
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
            val ssh = sshRepo.find().awaitFirst()

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

        val privatekey: String? = sshConfig.privatekey
        if (!privatekey.isNullOrEmpty()) {
            jsch.addIdentity(File(privatekey).normalize().path)
            session = jsch.getSession(username, host, Integer.valueOf(sshConfig.port))

            log.info("Authenticate SSH via private key!")
        } else {
            session = jsch.getSession(
                    username, host, Integer.valueOf(sshConfig.port))
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

    /**
     * Execute commands via SSH
     * @param command Represents shell commands
     * @see Command
     */
    @NewSpan
    suspend fun execute(@SpanTag("ssh-command")command: CommandInf): SshResponse {
        log.trace("Execute {}", command)
        if(!this::session.isInitialized || !session.isConnected) {
            initSSH()
        }

        val executionChannel: ChannelExec = try {
            session.openChannel("exec") as ChannelExec
        } catch (shhEx: JSchException) {
            log.error("SSH Session is down. Will try to reconnect.")
            initSSH()
            session.openChannel("exec") as ChannelExec
        }

        try {
            executionChannel.setCommand(command.parameters.joinToString(" "))
            val responseStream = ByteArrayOutputStream()
            val errorResponseStream = ByteArrayOutputStream()
            executionChannel.outputStream = responseStream
            executionChannel.setErrStream(errorResponseStream)

            try {
                executionChannel.connect()
            } catch (shhEx: JSchException) {
                log.error("SSH Session is down. Will try to reconnect.")
                initSSH()
            }

            // When then channel close itself, we retrieved the data
            while (executionChannel.isConnected) {
                delay(20)
            }

            val stdout = String(responseStream.toByteArray()).trim()
            var stderr = String(errorResponseStream.toByteArray()).trim()
            if (stdout.isNotEmpty()) {
                log.trace("Stdout: $stdout")
            }
            if (stderr.isNotEmpty() && executionChannel.exitStatus != 0) {
                val scriptErr =
                    """${if (command.shellScript.name.isNotEmpty()) "Script: ${command.shellScript}" else ""}
                        Command: ${command.parameters.joinToString(" ")}
                        Script error: $stderr
                        terminated with exit code: ${executionChannel.exitStatus}
                    """.trim()
                colored {
                    log.error(scriptErr.red)
                }
                stderr = """
                        Script error: $stderr
                        terminated with exit code: ${executionChannel.exitStatus}
                    """.trim()
            }
            val sshResponse = SshResponse(stdout, stderr)
            log.trace(sshResponse.toString())

            return sshResponse
        } finally {
            executionChannel.disconnect()
        }
    }

    /**
     * Simple upload from to
     * @param from from where the file is taken
     * @param to the path where the file will be uploaded
     * @see File
     */
    suspend fun upload(from: File, to: String) {
        if (!session.isConnected) {
            initSSH()
        }

        val normalizedTo = normalizeRemotePath(to)

        val sftpChannel: Channel = session.openChannel("sftp") as Channel
        try {
            if (!sftpChannel.isConnected) {
                sftpChannel.connect()
            }

            val sftp = sftpChannel as ChannelSftp
            log.debug("Upload file $from to $to")

            val os = System.getProperty("os.name")
            if (os.lowercase().contains("windows")) {
                sftp.put(from.canonicalPath, normalizedTo, ChannelSftp.OVERWRITE)
            } else {
                sftp.put(from.toURI().toURL().openStream(), normalizedTo, ChannelSftp.OVERWRITE)
            }
        } finally {
            sftpChannel.disconnect()
        }
    }

    /**
     * Upload a shell script via SFTP to SpaceUp temp directory and execute it optionally
     * @param cmd Contains a shell command and optionally a sftp file which can be executed
     * @see Command
     */
    @OptIn(DelicateCoroutinesApi::class)
    @NewSpan
    suspend fun upload(@SpanTag("ssh-command") cmd: CommandInf): SshResponse {
        log.debug("Upload $cmd")
        if(!session.isConnected) {
            initSSH()
        }

        val file = cmd.shellScript
        val remotefile = normalizeRemoteTempPath(file.name)

        val sftpChannel: Channel = session.openChannel("sftp") as Channel
        var sshResponse = SshResponse("", "")
        try {
            if(!sftpChannel.isConnected) {
                sftpChannel.connect()
            }

            val sftp = sftpChannel as ChannelSftp
            log.debug("Upload file ${file.name} to $remotefile")

            val os = System.getProperty("os.name")
            if(os.lowercase().contains("windows")) {
                val localScript = file.scriptPath?.file ?: ""
                sftp.put(File(localScript).canonicalPath, remotefile, ChannelSftp.OVERWRITE)
            } else {
                sftp.put(file.scriptPath?.openStream(), remotefile, ChannelSftp.OVERWRITE)
            }

            if(file.execute) {
                sshResponse = execute(cmd)
                // If set, we want to clear the file we have executed
                if(cmd.shellScript.clearAfterExecution) {
                    GlobalScope.launch {
                        log.debug("Remove remote file: $remotefile")
                        execute(Command(mutableListOf("rm", remotefile)))
                    }
                }
            }

            log.trace(sshResponse.toString())
            return sshResponse
        } finally {
            sftpChannel.disconnect()
        }
    }

    /**
     * normalize the remote file path with actual remote path, according to SSH user
     * @param filename where the remote file will be located
     */
    private suspend fun normalizeRemoteTempPath(filename: String): String {
        val sshRepo = dbService.getDb().getCollection<Ssh>()
        val ssh = sshRepo.find().awaitFirst()
        return spaceupRemotePathConfig.temp.replace("~", "/home/${ssh.username}") + "/$filename"
    }

    private suspend fun normalizeRemotePath(filepath: String): String {
        val sshRepo = dbService.getDb().getCollection<Ssh>()
        val ssh = sshRepo.find().awaitFirst()
        return filepath.replace("~", "/home/${ssh.username}")
    }
}