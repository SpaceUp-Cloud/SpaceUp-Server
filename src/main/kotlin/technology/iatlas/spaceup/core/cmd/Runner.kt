package technology.iatlas.spaceup.core.cmd

import io.micronaut.context.env.Environment
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class Runner<T>(private val env: Environment) : RunnerInf<T> {
    private val log: Logger = LoggerFactory.getLogger(Runner::class.java)

    override fun execute(cmd: CommandInf, parser: ParserInf<T>): T? {

        val preCommand: MutableList<String> = if (env.activeNames.contains("dev"))
            mutableListOf("bash") else mutableListOf()

        if(env.activeNames.contains("dev")) {
            log.warn("Get fake file in dev mode!")
            var fileNameToRetrieve = "output"
            cmd.parameters.forEach {
                fileNameToRetrieve += "_$it"
            }

            val file = this::class.java.getResource("/fakecmd/$fileNameToRetrieve.txt").path
            val outputFile = File(file)
            log.debug("File to get: {}", outputFile)

            return parser.parse(outputFile.readText())
        }

        val actualCmd = preCommand + cmd.parameters
        log.debug("Actual cmd: {} ", actualCmd)

        val processBuilder = ProcessBuilder()
        processBuilder.command(actualCmd)
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)

        val proc = processBuilder.start()
        proc.waitFor()

        val output = proc.inputStream.bufferedReader().readText()
        log.debug(output)

        return parser.parse(output)

    }

}