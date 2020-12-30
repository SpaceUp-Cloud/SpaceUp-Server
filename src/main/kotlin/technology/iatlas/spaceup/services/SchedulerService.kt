package technology.iatlas.spaceup.services

import io.micronaut.context.env.Environment
import io.micronaut.scheduling.annotation.Scheduled
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.dto.Command
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.core.parser.EchoParser
import technology.iatlas.spaceup.dto.UpdatePackage
import javax.inject.Singleton

@Singleton
class SchedulerService(
    private val domainService: DomainService,
    private val env: Environment) {
    private val log = LoggerFactory.getLogger(SchedulerService::class.java)

    @Scheduled(fixedDelay = "\${spaceup.scheduler.domains.update}")
    internal fun updateDomainList() {
        runBlocking {
            domainService.updateDomainList()
        }
    }

}