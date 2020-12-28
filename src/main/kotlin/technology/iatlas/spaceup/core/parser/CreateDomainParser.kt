package technology.iatlas.spaceup.core.parser

import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.cmd.ParserInf
import technology.iatlas.spaceup.dto.Feedback
import java.io.BufferedReader

class CreateDomainParser : ParserInf<Feedback> {
    private val log = LoggerFactory.getLogger(CreateDomainParser::class.java)

    override fun parse(cmdOutput: BufferedReader): Feedback {
        return parseText(cmdOutput.readText())
    }

    override fun parseText(cmdOutput: String): Feedback {
        log.trace("Output: $cmdOutput")

        return if(cmdOutput.toLowerCase().contains("error")) {
            Feedback("", cmdOutput)
        } else {
            Feedback("Domain was successfully created", "")
        }
    }
}
