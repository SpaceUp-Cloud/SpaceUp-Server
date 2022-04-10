package technology.iatlas.spaceup.services

import io.micronaut.context.annotation.Context
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.core.parser.EchoParser
import technology.iatlas.spaceup.core.parser.NetworkListenerParser
import technology.iatlas.spaceup.dto.Command
import technology.iatlas.spaceup.dto.NetworkProgram

@Context
class NetworkService(
    sshService: SshService,
    private val wsBroadcaster: WsBroadcaster
): WsServiceInf {

    override val topic = "network"

    private val networkReadRunner = Runner<List<NetworkProgram>>(sshService)
    private val processRunner = Runner<String>(sshService)

    suspend fun readListeningPrograms(): MutableList<NetworkProgram> {
        val networkConnectionsCmd = mutableListOf("netstat", "-tlnp")

        val networkPrograms: MutableList<NetworkProgram> = mutableListOf()
        networkReadRunner.subject().subscribe {
            networkPrograms.addAll(it)
        }

        networkReadRunner.execute(Command(networkConnectionsCmd), NetworkListenerParser())
        return networkPrograms
    }

    suspend fun getProcess(pid: Int): String {
        var program = ""
        val getProgramByPID = mutableListOf("ps", "-p", "$pid", "-o", "args", "--no-headers")
        processRunner.subject().subscribe {
            program = it
        }

        processRunner.execute(Command(getProgramByPID), EchoParser())
        return program
    }
}