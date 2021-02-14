package technology.iatlas.spaceup.core.parser

import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.cmd.ParserInf
import technology.iatlas.spaceup.core.cmd.SshResponse
import technology.iatlas.spaceup.dto.Feedback
import java.io.BufferedReader

class CreateDomainParser(private val domain: String) : ParserInf<Feedback> {
    private val log = LoggerFactory.getLogger(CreateDomainParser::class.java)

    override fun parseProcessOutput(processResponse: BufferedReader): Feedback {
        return parseSshOutput(processResponse.readText())
    }

    override fun parseSshOutput(sshResponse: SshResponse): Feedback {
        log.debug(sshResponse.toString())

        return if(sshResponse.stdout.isEmpty()) {
            Feedback("$domain was successfully created", "")
        } else {
            return if(sshResponse.stderr.isNotBlank() || sshResponse.stderr.isNotEmpty()) {
                Feedback("", "$domain: ${sshResponse.stderr}")
            } else {
                Feedback("", "Unexpected error happened!")
            }
        }

    }
}
