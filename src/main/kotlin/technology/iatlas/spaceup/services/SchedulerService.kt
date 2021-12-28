package technology.iatlas.spaceup.services

import io.micronaut.context.annotation.Context
import io.micronaut.scheduling.annotation.Scheduled
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@Context
class SchedulerService(
    private val domainService: DomainService,
    private val dbService: DbService
    ) {
    private val log = LoggerFactory.getLogger(SchedulerService::class.java)

    @Scheduled(fixedRate = "\${spaceup.scheduler.domains.update}", initialDelay = "\${spaceup.scheduler.delayed}")
    internal fun updateDomainList() {
        if(dbService.isAppInstalled()) {
            log.info("Update domain list")
            runBlocking {
                domainService.updateDomainList()
                domainService.list()
            }
        }


    }

}