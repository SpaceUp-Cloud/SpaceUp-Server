package technology.iatlas.spaceup.services

import io.micronaut.context.env.Environment
import io.micronaut.scheduling.annotation.Scheduled
import io.micronaut.tracing.annotation.ContinueSpan
import kotlinx.coroutines.GlobalScope
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.cmd.Command
import technology.iatlas.spaceup.core.cmd.CommandInf
import technology.iatlas.spaceup.core.cmd.ParserInf
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.core.parser.EchoParser
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

    //@Scheduled(fixedRate = "2m", initialDelay = "1s")
    internal fun checkServices() {
        log.debug("checkServices")

        val cmd: MutableList<String> = mutableListOf("uberspace", "web", "domain", "list")
        val result: String? = Runner<String>(env).execute(Command(cmd), EchoParser())
        log.info(result)
    }

}