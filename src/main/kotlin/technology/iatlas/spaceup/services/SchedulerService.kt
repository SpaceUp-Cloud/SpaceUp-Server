package technology.iatlas.spaceup.services

import io.micronaut.context.env.Environment
import io.micronaut.scheduling.annotation.Scheduled
import kotlinx.coroutines.GlobalScope
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.cmd.Command
import technology.iatlas.spaceup.core.cmd.CommandInf
import technology.iatlas.spaceup.core.cmd.ParserInf
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.dto.UpdatePackage
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class SchedulerService(
    private val sseService: SseServiceImpl<UpdatePackage>,
    private val env: Environment) {
    private val log = LoggerFactory.getLogger(SchedulerService::class.java)

    init {
        sseService.eventName = "update"
    }

    /*@Scheduled(
        fixedRate = "\${spaceup.scheduler.updates:5m}",
        initialDelay = "\${spaceup.scheduler.delayed:3s}"
    )*/
    internal fun checkUpdates() {
        val updatePackage = UpdatePackage(
            "Searx", "https://searx.org", "1.0.0"
        )
        log.debug("Update found: {}", updatePackage)
        sseService.publish(updatePackage)
    }

    //@Scheduled(fixedRate = "30s", initialDelay = "1s")
    internal fun checkConsoleOutputTest() {
        val processBuilder = ProcessBuilder()
        processBuilder.command("bash.exe", "-c", "ls -la")

        val proc = processBuilder.start()
        proc.waitFor(30, TimeUnit.SECONDS)

        val output = proc.inputStream.bufferedReader().readText()
        log.info(output)
    }

    @Scheduled(fixedRate = "5s", initialDelay = "1s")
    internal fun checkServices() {
        log.debug("checkServices")

        class EchoParser : ParserInf<String> {
            override fun parse(input: String): String {
                return input
            }
        }

        val cmd: MutableList<String> = mutableListOf("uberspace", "web", "domain", "list")
        val result: String? = Runner<String>(env).execute(Command(cmd), EchoParser())
        log.info(result)
    }

}