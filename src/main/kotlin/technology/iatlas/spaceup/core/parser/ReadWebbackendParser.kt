package technology.iatlas.spaceup.core.parser

import org.slf4j.LoggerFactory
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
                val process = splittedResponse[1]

                // sanitize credentials
                val regex = Regex("://(.*:.*)@")
                val service = splittedResponse[2].replace(regex, "://xxx:xxx@")

                val webbackendConfiguration = WebbackendConfiguration(web, process, service)
                configList.add(webbackendConfiguration)
            } else {
                // For / apache configuration as there are no processes/services behind
                if(splittedResponse[0].isNotEmpty()) {
                    val webbackendConfiguration = WebbackendConfiguration(
                        splittedResponse[0]
                    )
                    configList.add(webbackendConfiguration)
                }
            }
        }
        return configList
    }

}