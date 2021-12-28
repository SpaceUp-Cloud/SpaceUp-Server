package technology.iatlas.spaceup.controller.api

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import technology.iatlas.spaceup.core.annotations.Installed
import technology.iatlas.spaceup.dto.Disk
import technology.iatlas.spaceup.dto.Hostname
import technology.iatlas.spaceup.services.SystemService
import java.util.*

@Installed
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/system")
class SystemController(private val systemService: SystemService) {

    @Get("/hostname", produces = [MediaType.APPLICATION_JSON])
    suspend fun getHostname(): Hostname {
        return systemService.getHostname()
    }

    @Get("/disk", produces = [MediaType.APPLICATION_JSON])
    suspend fun getDiskUsage(): Disk {
        return systemService.getDiskQuota()
    }

    @Get("/version", produces = [MediaType.TEXT_PLAIN])
    fun getVersion(): String {
        val versionProperty = Properties()
        versionProperty.load(this.javaClass.getResourceAsStream("/version.properties"))
        return versionProperty.getProperty("version") ?: "no version"
    }
}