package technology.iatlas.spaceup.core.cmd

import io.micronaut.context.env.Environment
import io.micronaut.tracing.annotation.ContinueSpan
import io.micronaut.tracing.annotation.SpanTag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.services.SshService
import javax.inject.Singleton

@Singleton
open class Runner<T>(private val env: Environment,
                     private val sshService: SshService) : RunnerInf<T> {
    private val log: Logger = LoggerFactory.getLogger(Runner::class.java)

    private var devMode = env.activeNames.contains("dev")

    @ContinueSpan
    override fun execute(@SpanTag("runner.cmd") cmd: CommandInf, parser: ParserInf<T>): T? {
        log.trace("Actual cmd: {} ", cmd.parameters)

        return if (devMode) {
            val response = sshService.execute(cmd)
            parser.parseText(response)
        } else {
            val processBuilder = ProcessBuilder()
            processBuilder.command(cmd.parameters)
            processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)

            val proc = processBuilder.start()
            proc.waitFor()

            val output = proc.inputStream.bufferedReader()
            parser.parse(output)
        }
    }
}