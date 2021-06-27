package technology.iatlas.spaceup.core.parser

import technology.iatlas.spaceup.core.cmd.ParserInf
import technology.iatlas.spaceup.core.cmd.SshResponse
import technology.iatlas.spaceup.dto.Log
import java.io.BufferedReader

class LogsParser : ParserInf<Log> {
    override fun parseProcessOutput(processResponse: BufferedReader): Log {
        return parse(processResponse.readText())
    }

    override fun parseSshOutput(sshResponse: SshResponse): Log {
        return parse(sshResponse.stdout)
    }

    private fun parse(input: String): Log {
        val infoLogs: MutableList<String> = mutableListOf()
        val errorLogs: MutableList<String> = mutableListOf()
        val splittedLogs = input.split("---")

        splitup(splittedLogs.first()).forEach {
            infoLogs.add(it)
        }

        splitup(splittedLogs.last()).forEach {
            errorLogs.add(it)
        }

        return Log(infoLogs as ArrayList<String>, errorLogs as ArrayList<String>)
    }

    private fun splitup(l: String): Sequence<String> {
        return l.split("\n").filter {
            it != ""
        }.map {
            it.trim()
        }.asSequence()
    }
}