package technology.iatlas.spaceup.core.cmd

import io.micronaut.context.env.Environment
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Runner<T>(private val env: Environment) : RunnerInf<T> {
    private val log: Logger = LoggerFactory.getLogger(Runner::class.java)

    override fun execute(cmd: CommandInf, parser: ParserInf<T>): T? {
        val preCommand: MutableList<String> = if (env.activeNames.contains("dev"))
            mutableListOf("bash.exe", "-c") else mutableListOf()

        val actualCmd = preCommand + cmd.parameters
        log.debug("Actual cmd: {} ", actualCmd)

        val processBuilder = ProcessBuilder()
        processBuilder.command(actualCmd)
        processBuilder.redirectError()
        processBuilder.redirectInput()

        val proc = processBuilder.start()
        proc.waitFor()

        val output = proc.inputStream.bufferedReader().readText()
        log.info(output)

        return parser.parse(output)

    }

}