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

import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.cmd.ParserInf
import technology.iatlas.spaceup.core.cmd.SshResponse
import technology.iatlas.spaceup.dto.Service
import java.io.BufferedReader

class ServiceParser : ParserInf<List<Service>> {
    private val log = LoggerFactory.getLogger(ServiceParser::class.java)

    override fun parseProcessOutput(processResponse: BufferedReader): List<Service> {
        val serviceList = mutableListOf<Service>()

        processResponse.lines().forEach { line ->
            parse(line, serviceList)
        }

        return serviceList
    }

    override fun parseSshOutput(sshResponse: SshResponse): List<Service> {
        val serviceList = mutableListOf<Service>()

        sshResponse.stdout.split("\n").toList().filter {
            it != ""
        }.forEach{ line ->
            parse(line, serviceList)
        }

        return serviceList
    }

    private fun parse(line: String, serviceList: MutableList<Service>) {
        val splitted = line.replace("\\s+".toRegex(), " ").split(" ")

        val name = splitted[0]
        val status = splitted[1]
        var info = ""

        splitted.filter {
            it != name && it != status
        }.forEach {
            if(it.isNotBlank()) {
                info = "$info ${it.replace(",", "")}"
            }
        }

        val service = Service(name, status, info)
        log.trace("Service: $service")

        serviceList.add(service)
    }
}