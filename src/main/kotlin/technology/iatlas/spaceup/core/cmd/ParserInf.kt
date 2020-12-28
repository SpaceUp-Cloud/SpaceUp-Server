package technology.iatlas.spaceup.core.cmd

import java.io.BufferedReader

interface ParserInf<out T> {
    fun parse(cmdOutput: BufferedReader): T
    fun parseText(cmdOutput: String): T
}
