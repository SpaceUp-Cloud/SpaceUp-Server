package technology.iatlas.spaceup.core.parser

import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.cmd.ParserInf
import technology.iatlas.spaceup.dto.Domain
import java.io.BufferedReader

class DomainParser : ParserInf<List<Domain>> {
    private val log = LoggerFactory.getLogger(DomainParser::class.java)

    private val domainList = mutableListOf<Domain>()

    override fun parse(cmdOutput: BufferedReader): List<Domain>{
        cmdOutput.lines().forEach {
            if (it.isNotBlank()) {
                val domain = Domain(it)
                domainList.add(domain)
            }
        }

        return domainList
    }

    override fun parseText(cmdOutput: String): List<Domain> {
        cmdOutput.split("\n")
            .filter {
                it != ""
            }.forEach {
            if (it.isNotBlank()) {
                val domain = Domain(it)
                domainList.add(domain)
            }
        }

        log.trace("Domains: $domainList")
        return domainList
    }
}