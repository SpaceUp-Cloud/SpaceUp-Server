package technology.iatlas.spaceup.services

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Value
import kotlinx.coroutines.GlobalScope
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.config.SpaceUpSshConfig
import technology.iatlas.spaceup.core.cmd.CommandInf
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.Delegates

@Singleton
class SshService(private val sshConfig: SpaceUpSshConfig) {
    private val log = LoggerFactory.getLogger(SshService::class.java)

    private var session: Session

    init {
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

    fun execute(command: CommandInf): String {
        log.debug("Execute $command")
        val channel: ChannelExec = session.openChannel("exec") as ChannelExec

        try {
            channel.setCommand(command.parameters.joinToString(" "))
            val responseStream = ByteArrayOutputStream()
            channel.outputStream = responseStream
            channel.connect(10000)

            // When then channel close itself, we retrieved the data
            while (channel.isConnected) {
                GlobalScope.run {
                    Thread.sleep(100)
                }
            }

            val response = String(responseStream.toByteArray())
            log.trace("Response:\n$response")

            return response
        } finally {
            //session.disconnect()
            channel.disconnect()
        }
    }
}