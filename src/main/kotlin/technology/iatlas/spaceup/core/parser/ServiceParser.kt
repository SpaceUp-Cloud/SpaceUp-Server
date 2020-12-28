package technology.iatlas.spaceup.core.parser

import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.cmd.ParserInf
import technology.iatlas.spaceup.dto.Service
import java.io.BufferedReader

class ServiceParser : ParserInf<List<Service>> {
    private val log = LoggerFactory.getLogger(ServiceParser::class.java)

    override fun parse(cmdOutput: BufferedReader): List<Service> {
        val serviceList = mutableListOf<Service>()

        cmdOutput.lines().forEach { line ->
            parse(line, serviceList)
        }

        return serviceList
    }

    override fun parseText(cmdOutput: String): List<Service> {
        val serviceList = mutableListOf<Service>()

        cmdOutput.split("\n").toList().filter {
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