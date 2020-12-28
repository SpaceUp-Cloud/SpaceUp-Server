package technology.iatlas.spaceup.services

import io.micronaut.context.env.Environment
import io.micronaut.scheduling.annotation.Scheduled
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.dto.Command
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.core.parser.EchoParser
import technology.iatlas.spaceup.dto.UpdatePackage
import javax.inject.Singleton

@Singleton
class SchedulerService(
    private val sseService: SseService<UpdatePackage>,
    private val sshService: SshService,
    private val domainService: DomainService,
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
        val result: String? = Runner<String>(env, sshService).execute(Command(cmd), EchoParser())
        log.info(result)
    }

    @Scheduled(fixedRate = "1m")
    internal fun updateDomainList() {
        domainService.updateDomainList()
    }

}