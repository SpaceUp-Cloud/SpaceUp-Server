package technology.iatlas.spaceup.core.cmd

import io.micronaut.context.env.Environment
import io.micronaut.tracing.annotation.ContinueSpan
import io.micronaut.tracing.annotation.SpanTag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.*
import java.net.URL

class Runner<T>(private val env: Environment) : RunnerInf<T> {
    private val log: Logger = LoggerFactory.getLogger(Runner::class.java)

    @ContinueSpan
    override fun execute(@SpanTag("runner.cmd") cmd: CommandInf, parser: ParserInf<T>): T? {

        val preCommand: MutableList<String> = if (env.activeNames.contains("dev"))
            mutableListOf("bash") else mutableListOf()

        if(env.activeNames.contains("dev")) {
            log.warn("Get fake file in dev mode!")
            var fileNameToRetrieve = "output"
            cmd.parameters.forEach {
                fileNameToRetrieve += "_$it"
            }

            // dummy file if resource does not exist
            val url =
                this::class.java.getResource("/fakecmd/$fileNameToRetrieve.txt") ?:
                this::class.java.getResource("/fakecmd/dummy.txt")

            val outputFile = File(url.path)
            log.debug("File to get: {}", outputFile)

            return parser.parse(outputFile.bufferedReader())
        }

        val actualCmd = preCommand + cmd.parameters
        log.debug("Actual cmd: {} ", actualCmd)

        val processBuilder = ProcessBuilder()
        processBuilder.command(actualCmd)
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)

        val proc = processBuilder.start()
        proc.waitFor()

        val output = proc.inputStream.bufferedReader()
        return parser.parse(output)

    }

}