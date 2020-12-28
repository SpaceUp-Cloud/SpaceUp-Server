package technology.iatlas.spaceup.core.parser

import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.cmd.ParserInf
import java.io.BufferedReader

class EchoParser : ParserInf<String> {
    private val log = LoggerFactory.getLogger(EchoParser::class.java)

    override fun parse(cmdOutput: BufferedReader): String {
        return parseText(cmdOutput.readText())
    }

    override fun parseText(cmdOutput: String): String {
        log.trace("Echo output: $cmdOutput")
        return cmdOutput
    }
}