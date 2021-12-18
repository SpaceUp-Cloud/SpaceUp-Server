package technology.iatlas.spaceup.services

import io.micronaut.context.annotation.Context
import io.micronaut.scheduling.annotation.Scheduled
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@Context
class SchedulerService(
    private val domainService: DomainService,
    private val serviceService: ServiceService
    ) {
    private val log = LoggerFactory.getLogger(SchedulerService::class.java)

    @Scheduled(fixedDelay = "\${spaceup.scheduler.domains.update}")
    internal fun updateDomainList() {
        log.debug("Update domain list")
        runBlocking {
            domainService.updateDomainList()
            domainService.list()
        }
    }

    @Scheduled(fixedDelay = "\${spaceup.scheduler.services.update}", initialDelay = "\${spaceup.scheduler.delayed}")
    internal fun updateServices() {
        log.debug("Update service list")
        runBlocking {
            serviceService.list()
        }
    }

}