package technology.iatlas.spaceup.core.cmd

import io.micronaut.context.env.Environment
import io.micronaut.tracing.annotation.ContinueSpan
import io.micronaut.tracing.annotation.SpanTag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.*
import java.net.URL
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
open class Runner<T>(private val env: Environment) : RunnerInf<T> {
    private val log: Logger = LoggerFactory.getLogger(Runner::class.java)

    private var devMode = false

    init {
        devMode = env.activeNames.contains("dev")
        if (devMode) {
            log.warn("Get fake file in dev mode!")
        }
    }

    @ContinueSpan
    override fun execute(@SpanTag("runner.cmd") cmd: CommandInf, parser: ParserInf<T>): T? {

        val preCommand: MutableList<String> = if (env.activeNames.contains("dev"))
            mutableListOf("bash") else mutableListOf()

        if (devMode) {

            // simulate some long running
            val rand = Random(1000)
            val workTime = (2000..5000).random(rand).toLong()
            Thread.sleep(workTime)
            log.debug("Sleep for $workTime")

            var fileNameToRetrieve = "output"
            cmd.parameters.forEach {
                fileNameToRetrieve += "_$it"
            }

            log.debug("File to retrieve: $fileNameToRetrieve")

            // dummy file if resource does not exist
            val url =
                this::class.java.getResource("/fakecmd/$fileNameToRetrieve.txt")
                    ?: this::class.java.getResource("/fakecmd/dummy.txt")

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