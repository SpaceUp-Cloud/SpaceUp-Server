/*
 * Copyright (c) 2022 spaceup@iatlas.technology.
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
 *
 * There is a strong belief within us that the license we have chosen provides not only the best solution for providing you with the essential freedom necessary to use <project name> within your projects, but also for maintaining enough copyleft strength for us to feel confident and secure with releasing our hard work to the public. For your convenience we've included our own interpretation of the license we chose, which can be seen below.
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
 *             This is due to those licenses classifying <project name> as a work which would fall under an "aggregate" work by their terms and definitions;
 *             as such it should not be covered by their terms and conditions. The relevant passages start at:
 *             - Line 129 of the GNU General Public License version 2
 *             - Line 206 of the GNU Lesser General Public License version 2.1
 *      b) If you have not "modified", "adapted" or "extended" SpaceUp-Server then your work should not be bound by this license,
 *         as you are using <project name> under the definition of an "aggregate" work.
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

import com.jcraft.jsch.*
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Value
import kotlinx.coroutines.delay
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
            log.info("Take saved credentials")
            val db = dbService.getDb()
            val sshRepo = db.getCollection<Ssh>()
            log.info("Assuming there is only one configuration")
            val ssh = sshRepo.findOne()!!

            securityService.decrypt(ssh) {
                username = ssh.username
                password = ssh.password
                host = ssh.server
            }
        }

        if((sshConfig.privatekey == null || sshConfig.privatekey!!.isEmpty())) {
            colored {
                log.warn("To authenticate with Privatekey supply" +
                        " '-spaceup.ssh.privatekey=\"your path to key\"' ".yellow + "to JAR.")
            }
        }

        if(sshConfig.port == null) {
            log.error("Provide SSH port '-spaceup.ssh.port=22' to JAR.")
        }

        val privatekey: String? = sshConfig.privatekey
        if (privatekey != null && privatekey.isNotEmpty()) {
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
        log.debug("Execute $command")
        if(!this::session.isInitialized || !session.isConnected) {
            initSSH()
        }

        var channel: ChannelExec
        try {
            channel = session.openChannel("exec") as ChannelExec
        } catch (shhEx: JSchException) {
            log.error("SSH Session is down. Will try to reconnect.")
            initSSH()
            channel = session.openChannel("exec") as ChannelExec
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

            val response = String(responseStream.toByteArray())
            val error = String(errorResponseStream.toByteArray())
            val sshResponse = SshResponse(response, error)
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

        val writeScriptChannel: Channel = session.openChannel("sftp") as Channel

        val executionChannel: ChannelExec = session.openChannel("exec") as ChannelExec
        val executeCmd = cmd.parameters.joinToString(" ")
        executionChannel.setCommand(executeCmd)

        val responseExecution = ByteArrayOutputStream()
        val errorExecution = ByteArrayOutputStream()
        executionChannel.outputStream = responseExecution
        executionChannel.setErrStream(errorExecution)

        var sshResponse = SshResponse("", "")
        try {
            writeScriptChannel.connect()
            val sftp = writeScriptChannel as ChannelSftp
            log.info("Upload script ${file.name} to $remotefile")
            sftp.put(file.scriptPath?.openStream(), remotefile, ChannelSftp.OVERWRITE)

            if(file.doExecuteFile) {
                log.debug("Execute $executeCmd")
                executionChannel.connect()

                // When then channel close itself, we retrieved the data
                while (executionChannel.isConnected) {
                    delay(50)
                }

                sshResponse = SshResponse(String(responseExecution.toByteArray()), String(errorExecution.toByteArray()))
                // SshResponse
                //log.debug(sshResponse.toString())
            }

            log.trace(sshResponse.toString())
            return sshResponse
        } finally {
            writeScriptChannel.disconnect()
            executionChannel.disconnect()
        }
    }
}