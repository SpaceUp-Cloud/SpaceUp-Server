package technology.iatlas.spaceup.services

import io.micronaut.context.env.Environment
import io.micronaut.scheduling.annotation.Scheduled
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class SchedulerService(
    private val domainService: DomainService,
    private val serviceService: ServiceService,
    private val env: Environment,
    private val wsBroadcaster: WsBroadcaster) {
    private val log = LoggerFactory.getLogger(SchedulerService::class.java)

    @Scheduled(fixedDelay = "\${spaceup.scheduler.domains.update}")
    internal fun updateDomainList() {
        runBlocking {
            domainService.updateDomainList()
            domainService.list()
        }
    }

    @Scheduled(fixedDelay = "\${spaceup.scheduler.services.update}")
    internal fun updateServices() {
        runBlocking {
            serviceService.list()
        }
    }

}