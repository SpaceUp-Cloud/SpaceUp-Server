package technology.iatlas.spaceup.core.cmd

import io.micronaut.context.env.Environment
import io.micronaut.tracing.annotation.ContinueSpan
import io.micronaut.tracing.annotation.SpanTag
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.services.SshService
import java.io.BufferedReader

@Singleton
open class Runner<T>(
    env: Environment,
    private val sshService: SshService) : BaseRunner<T>() {
    private val log: Logger = LoggerFactory.getLogger(Runner::class.java)

    private var devMode = env.activeNames.contains("dev")

    @ContinueSpan
    override suspend fun execute(@SpanTag("runner.cmd") cmd: CommandInf, parser: ParserInf<T>) {
        log.trace("Actual cmd: {} ", cmd.parameters)

        val script = cmd.shellScript
        val output = if(script.name == "") {
            val res = sshService.execute(cmd)
            parser.parseSshOutput(res)
        } else {
            val res = sshService.upload(cmd)
            parser.parseSshOutput(res)
        }
        if(devMode) {
            log.debug("Parsed output: $output")
        }
        subject.onNext(output)
    }
}