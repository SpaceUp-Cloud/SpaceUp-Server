package technology.iatlas.spaceup.controller.api

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import technology.iatlas.spaceup.dto.Disk
import technology.iatlas.spaceup.dto.Hostname
import technology.iatlas.spaceup.services.SystemService

@Controller("/api/system")
class SystemController(private val systemService: SystemService) {

    @Get("/hostname", produces = [MediaType.APPLICATION_JSON])
    suspend fun getHostname(): Hostname {
        return systemService.getHostname()
    }

    @Get("/disk", produces = [MediaType.APPLICATION_JSON])
    suspend fun getDiskUsage(): Disk? {
        return systemService.getDiskQuota()
    }
}