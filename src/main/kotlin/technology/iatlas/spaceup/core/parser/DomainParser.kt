package technology.iatlas.spaceup.core.parser

import technology.iatlas.spaceup.core.cmd.ParserInf
import technology.iatlas.spaceup.dto.Domains
import java.io.BufferedReader

class DomainParser : ParserInf<Domains> {
    private val domainList = mutableListOf<String>()
    override fun parse(cmdOutput: BufferedReader): Domains{
        cmdOutput.lines().forEach {
            if (it.isNotBlank()) {
                domainList.add(it)
            }
        }

        return Domains(domainList)
    }
}