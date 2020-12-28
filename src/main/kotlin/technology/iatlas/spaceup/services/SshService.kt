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
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.Delegates

@Singleton
class SshService(private val sshConfig: SpaceUpSshConfig) {
    private val log = LoggerFactory.getLogger(SshService::class.java)

    fun execute(command: CommandInf): String {
        log.debug("Execute $command")

        lateinit var session: Session
        lateinit var channel: ChannelExec

        try {
            var port = 22
            if(sshConfig.port != null) {
                port = Integer.valueOf(sshConfig.port!!)
            }
            session = JSch().getSession(sshConfig.username,
                sshConfig.host, port)
            session.setPassword(sshConfig.password)
            session.setConfig("StrictHostKeyChecking", "no")
            session.connect()

            channel = session.openChannel("exec") as ChannelExec
            channel.setCommand(command.parameters.joinToString(" "))
            val responseStream = ByteArrayOutputStream()
            channel.outputStream = responseStream
            channel.connect(10000)

            while(channel.isConnected) {
                GlobalScope.run {
                    Thread.sleep(100)
                }
            }

            val response = String(responseStream.toByteArray())
            log.debug("Response:\n$response")

            return response
        } finally {
            session.disconnect()
            channel.disconnect()
        }
    }
}