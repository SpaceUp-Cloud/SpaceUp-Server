package technology.iatlas.spaceup.core.parser

import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.cmd.ParserInf
import technology.iatlas.spaceup.core.cmd.SshResponse
import java.io.BufferedReader

class EchoParser : ParserInf<String> {
    private val log = LoggerFactory.getLogger(EchoParser::class.java)

    override fun parseProcessOutput(processResponse: BufferedReader): String {
        return processResponse.readText()
    }

    override fun parseSshOutput(sshResponse: SshResponse): String {
        log.trace("Echo output: $sshResponse")
        return if(sshResponse.stderr.isNotEmpty()) {
            sshResponse.stderr
        } else {
            sshResponse.stdout
        }
    }
}