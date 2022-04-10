package technology.iatlas.spaceup.services

import io.micronaut.context.annotation.Context
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.core.parser.EchoParser
import technology.iatlas.spaceup.dto.Command

@Context
class ProcessService(
    private val sshService: SshService,
    private val wsBroadcaster: WsBroadcaster
): WsServiceInf {

    override val topic = "process"
    private val processRunner = Runner<String>(sshService)

    suspend fun getProcess(pid: Int): String {
        var program = ""
        val getProgramByPID = mutableListOf("ps", "-p", "$pid", "-o", "args", "--no-headers")
        processRunner.subject().subscribe {
            wsBroadcaster.broadcast(it, topic)
            program = it
        }

        processRunner.execute(Command(getProgramByPID), EchoParser())
        return program
    }
}