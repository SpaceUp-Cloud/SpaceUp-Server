package technology.iatlas.spaceup.services

import io.micronaut.scheduling.annotation.Scheduled
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.dto.UpdatePackage
import java.time.LocalDateTime
import javax.inject.Singleton

@Singleton
class SchedulerService(private val sseService: SseServiceImpl<UpdatePackage>) {
    private val log = LoggerFactory.getLogger(SchedulerService::class.java)

    init {
        sseService.eventName = "update"
    }

    @Scheduled(fixedRate = "\${spaceup.scheduler.updates:5m}",
        initialDelay = "\${spaceup.scheduler.delayed:3s}")
    internal fun checkUpdates() {
        val updatePackage = UpdatePackage("Searx",
            "https://searx.org", "1.0.0")
        log.debug("Update found: {}", updatePackage)
        sseService.publish(updatePackage)
    }
}