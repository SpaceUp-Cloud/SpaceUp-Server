package technology.iatlas.spaceup.core.parser

import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.cmd.ParserInf
import technology.iatlas.spaceup.dto.Feedback
import java.io.BufferedReader

class CreateDomainParser(private val domain: String) : ParserInf<Feedback> {
    private val log = LoggerFactory.getLogger(CreateDomainParser::class.java)

    private val errorList = listOf("error", "failure", "can't")

    override fun parse(cmdOutput: BufferedReader): Feedback {
        return parseText(cmdOutput.readText())
    }

    override fun parseText(cmdOutput: String): Feedback {
        log.debug(cmdOutput)

        // Check if the output contains any error or unwanted word
        val mappedOutput = cmdOutput.split(" ").map { it.toLowerCase() }
        val errorStringFound: String? = errorList.find { error -> mappedOutput.contains(error) }

        return if(errorStringFound.isNullOrEmpty() && cmdOutput.isNotBlank() && !cmdOutput.isNullOrEmpty()) {
            Feedback("$domain was successfully created", "")
        } else {
            return if(cmdOutput.isNotBlank() || cmdOutput.isNotEmpty()) {
                Feedback("", "$domain: $cmdOutput")
            } else {
                Feedback("", "Unexpected error happened!")
            }
        }
    }
}
