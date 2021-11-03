package technology.iatlas.spaceup.services

import com.jcraft.jsch.*
import jakarta.inject.Singleton
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.config.SpaceUpSftpConfig
import technology.iatlas.spaceup.config.SpaceUpSshConfig
import technology.iatlas.spaceup.core.cmd.CommandInf
import technology.iatlas.spaceup.core.cmd.SshResponse
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStreamReader

@Singleton
class SshService(
    private val sshConfig: SpaceUpSshConfig,
    private val sftpConfig: SpaceUpSftpConfig
    ) {
    private val log = LoggerFactory.getLogger(SshService::class.java)

    private lateinit var session: Session

    // Configure SSH
    init {
        initSSH()
    }

    private fun initSSH() {
        val jsch = JSch()

        val privatekey: String? = sshConfig.privatekey
        if (privatekey != null && privatekey.isNotEmpty()) {
            jsch.addIdentity(File(privatekey).normalize().path)
            session = jsch.getSession(
                    sshConfig.username, sshConfig.host, Integer.valueOf(sshConfig.port!!))

            log.info("Authenticate SSH via private key!")
        } else {
            session = jsch.getSession(
                    sshConfig.username, sshConfig.host, Integer.valueOf(sshConfig.port!!))
            session.setPassword(sshConfig.password)

            log.info("Authenticate SSH via password!")
        }
        session.setConfig("StrictHostKeyChecking", "no")
        session.connect()
    }

    suspend fun execute(command: CommandInf): SshResponse {
        log.debug("Execute $command")
        if(!session.isConnected) {
            initSSH()
        }

        val channel: ChannelExec = session.openChannel("exec") as ChannelExec

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
            // SshResponse
            log.debug(sshResponse.toString())

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
        val file = cmd.shellScript
        val remotefile = sftpConfig.remotedir + "/" + file.name

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
            sftp.put(file.scriptPath?.openStream(), remotefile, ChannelSftp.OVERWRITE)

            if(file.doExecute) {
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

            return sshResponse
        } finally {
            writeScriptChannel.disconnect()
            executionChannel.disconnect()
        }
    }
}