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
import technology.iatlas.spaceup.dto.WebbackendConfiguration
import java.io.BufferedReader

class ReadWebbackendParser: ParserInf<List<WebbackendConfiguration>> {
    override fun parseProcessOutput(processResponse: BufferedReader): List<WebbackendConfiguration> {
        TODO("Not yet implemented")
    }

    override fun parseSshOutput(sshResponse: SshResponse): List<WebbackendConfiguration> {
        val configList = mutableListOf<WebbackendConfiguration>()

        val splittedLines = sshResponse.stdout.split("\n")
        splittedLines.forEach {
            val splittedResponse = it.split(",").map { p -> p.trim() }
            if(splittedResponse.size == 3) {
                val web = splittedResponse[0]
                val process = splittedResponse[1].replace("listening: ", "")

                // sanitize credentials
                val regex = Regex("://(.*:.*)@")
                val service = splittedResponse[2].replace(regex, "://xxx:xxx@")

                val webbackendConfiguration = WebbackendConfiguration(web = web, process = process, service = service)
                configList.add(webbackendConfiguration)
            } else if(splittedResponse.size == 4) {
                val web = splittedResponse[0]
                val prefix = splittedResponse[1]
                val process = splittedResponse[2].replace("listening: ", "")

                // sanitize credentials
                val regex = Regex("://(.*:.*)@")
                val service = splittedResponse[3].replace(regex, "://xxx:xxx@")

                val webbackendConfiguration = WebbackendConfiguration(web, prefix, process, service)
                configList.add(webbackendConfiguration)
            } /*else { // Might be not useful to show
                // For / apache configuration as there are no processes/services behind
                if(splittedResponse[0].isNotEmpty()) {
                    val webbackendConfiguration = WebbackendConfiguration(
                        splittedResponse[0]
                    )
                    configList.add(webbackendConfiguration)
                }
            }*/
        }
        return configList
    }

}