/*
 * Copyright (c) 2022 Gino Atlas.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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