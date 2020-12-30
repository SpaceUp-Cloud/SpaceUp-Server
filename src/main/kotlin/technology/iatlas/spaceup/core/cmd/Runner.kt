package technology.iatlas.spaceup.core.cmd

import io.micronaut.context.env.Environment
import io.micronaut.tracing.annotation.ContinueSpan
import io.micronaut.tracing.annotation.SpanTag
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.services.SshService
import java.io.BufferedInputStream
import java.io.BufferedReader
import javax.inject.Singleton

@Singleton
open class Runner<T>(
    env: Environment,
    private val sshService: SshService) : BaseRunner<T>() {
    private val log: Logger = LoggerFactory.getLogger(Runner::class.java)

    private var devMode = env.activeNames.contains("dev")

    @ContinueSpan
    override suspend fun execute(@SpanTag("runner.cmd") cmd: CommandInf, parser: ParserInf<T>) {
        log.trace("Actual cmd: {} ", cmd.parameters)

        if (devMode) {
            val res = sshService.execute(cmd)
            val output = parser.parseText(res)
            subject.onNext(output)

        } else {
            val processBuilder = ProcessBuilder()
            processBuilder.command(cmd.parameters)
            processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)

            lateinit var res: BufferedReader
            GlobalScope.launch(Dispatchers.IO) {
                run {
                    val proc = processBuilder.start()
                    proc.waitFor()
                    res = proc.inputStream.bufferedReader()
                }
            }
            val output = parser.parse(res)
            subject.onNext(output)
        }
    }

}