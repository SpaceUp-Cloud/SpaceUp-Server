package technology.iatlas.spaceup.core.parser

import technology.iatlas.spaceup.core.cmd.ParserInf
import java.io.BufferedReader

class EchoParser : ParserInf<String> {
    override fun parse(cmdOutput: BufferedReader): String {
        return cmdOutput.readText()
    }
}