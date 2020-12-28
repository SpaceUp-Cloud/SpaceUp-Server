package technology.iatlas.spaceup.core.parser

import technology.iatlas.spaceup.core.cmd.ParserInf
import technology.iatlas.spaceup.dto.Feedback
import java.io.BufferedReader

class CreateDomainParser : ParserInf<Feedback> {
    override fun parse(cmdOutput: BufferedReader): Feedback {
        return parseText(cmdOutput.readText())
    }

    override fun parseText(cmdOutput: String): Feedback {
        return if(cmdOutput.toLowerCase().contains("error")) {
            Feedback("", cmdOutput)
        } else {
            Feedback("Domain was successfully created", "")
        }
    }
}
