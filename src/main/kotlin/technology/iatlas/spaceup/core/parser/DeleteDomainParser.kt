package technology.iatlas.spaceup.core.parser

import technology.iatlas.spaceup.core.cmd.ParserInf
import technology.iatlas.spaceup.core.cmd.SshResponse
import technology.iatlas.spaceup.dto.Feedback
import java.io.BufferedReader

class DeleteDomainParser(private val domain: String) : ParserInf<Feedback> {
    override fun parseProcessOutput(processResponse: BufferedReader): Feedback {
        val responseText = processResponse.readText()
        // Workaround: pack it into sshResponse
        var errorMsg = ""
        var infoMsg = ""

        if (responseText.lowercase().contains("error", true)) {
            errorMsg = responseText
        } else {
            infoMsg = responseText
        }

        return parseSshOutput(SshResponse(stdout = infoMsg, stderr = errorMsg))
    }

    override fun parseSshOutput(sshResponse: SshResponse): Feedback {
        return if(sshResponse.stderr.isNotEmpty()) {
            Feedback("", "$domain: ${sshResponse.stderr}")
        } else {
            Feedback("$domain was successfully deleted!", "")
        }
    }
}
