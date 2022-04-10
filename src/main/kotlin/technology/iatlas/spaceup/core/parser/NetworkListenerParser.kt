package technology.iatlas.spaceup.core.parser

import technology.iatlas.spaceup.core.cmd.ParserInf
import technology.iatlas.spaceup.core.cmd.SshResponse
import technology.iatlas.spaceup.dto.NetworkProgram
import java.io.BufferedReader

class NetworkListenerParser: ParserInf<List<NetworkProgram>> {
    override fun parseProcessOutput(processResponse: BufferedReader): List<NetworkProgram> {
        TODO("Not yet implemented")
    }

    override fun parseSshOutput(sshResponse: SshResponse): List<NetworkProgram> {
        val networkPrograms = mutableListOf<NetworkProgram>()
        val parsedLines = sshResponse.stdout.split("\n")
            .filter {
                it.contains(Regex("tcp|tcp6"))
            }.map {
                it.split(Regex("\\s+"))
            }

        parsedLines.forEach {
            netline ->
                if(netline[6] != "-") {
                    if(netline[3].contains("0.0.0.0")) {
                        val port = netline[3].split(":")[1]
                        val pid = netline[6].split("/")[0]
                        val program = netline[6].split("/")[1]

                        networkPrograms.add(
                            NetworkProgram(port = Integer.valueOf(port), pid = Integer.valueOf(pid), program = program)
                        )
                    } else if(netline[3].contains(":::")) {
                        val port = netline[3].split(":::")[1]
                        val pid = netline[6].split("/")[0]
                        val program = netline[6].split("/")[1]

                        networkPrograms.add(
                            NetworkProgram(port = Integer.valueOf(port), pid = Integer.valueOf(pid), program = program)
                        )
                    }
                }
        }

        return networkPrograms
    }
}