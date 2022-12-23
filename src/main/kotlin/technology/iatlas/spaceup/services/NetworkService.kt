package technology.iatlas.spaceup.services

import io.micronaut.context.annotation.Context
import io.micronaut.tracing.annotation.NewSpan
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.core.parser.NetworkListenerParser
import technology.iatlas.spaceup.dto.Command
import technology.iatlas.spaceup.dto.NetworkProgram

@Context
class NetworkService(
    private val sshService: SshService,
    private val processService: ProcessService,
    private val wsBroadcaster: WsBroadcaster
): WsServiceInf {

    override val topic = "network"

    @NewSpan
    suspend fun readListeningPrograms(): MutableList<NetworkProgram> {
        val networkReadRunner = Runner<List<NetworkProgram>>(sshService)
        val networkConnectionsCmd = mutableListOf("netstat", "-tlnp")

        val networkPrograms: MutableList<NetworkProgram> = mutableListOf()
        networkReadRunner.subject().subscribe {
            networkPrograms.addAll(it)
            wsBroadcaster.broadcast(it, topic)
        }

        networkReadRunner.execute(Command(networkConnectionsCmd), NetworkListenerParser())

        // Resolve process id and replace program with actual call
        networkPrograms.map {
            it.program = processService.getProgByProcess(it.pid).trim()
        }
        return networkPrograms
    }
}