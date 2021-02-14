package technology.iatlas.spaceup.core.cmd

import java.io.BufferedReader

interface ParserInf<out T> {
    /**
     * Parse the output of the ProcessBuilder
     */
    fun parseProcessOutput(processResponse: BufferedReader): T

    /**
     * Parse the output from SSH execution
     */
    fun parseSshOutput(sshResponse: SshResponse): T
}
