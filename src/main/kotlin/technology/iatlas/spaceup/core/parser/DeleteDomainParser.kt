package technology.iatlas.spaceup.core.parser

import technology.iatlas.spaceup.core.cmd.ParserInf
import technology.iatlas.spaceup.dto.Feedback
import java.io.BufferedReader

class DeleteDomainParser : ParserInf<Feedback> {
    override fun parse(cmdOutput: BufferedReader): Feedback {
        val responseText = cmdOutput.readText()
        return parseText(responseText)
    }

    override fun parseText(cmdOutput: String): Feedback {
        return if(cmdOutput.toLowerCase().contains("error")) {
            Feedback("", cmdOutput)
        } else {
            Feedback("Successful deleted!", "")
        }
    }

}
