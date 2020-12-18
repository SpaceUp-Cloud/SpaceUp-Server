package technology.iatlas.spaceup.core.parser

import technology.iatlas.spaceup.core.cmd.ParserInf
import technology.iatlas.spaceup.dto.Feedback
import java.io.BufferedReader

class CreateDomainParser : ParserInf<Feedback> {
    override fun parse(cmdOutput: BufferedReader): Feedback {
        val textOutput = cmdOutput.readText()
        return if(textOutput.toLowerCase().contains("error")) {
            Feedback("", textOutput)
        } else {
            Feedback("Domain was successfully created", "")
        }
    }
}
