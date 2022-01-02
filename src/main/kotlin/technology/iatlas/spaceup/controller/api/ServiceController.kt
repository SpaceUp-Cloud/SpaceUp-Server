/*
 * Copyright (c) 2022 Gino Atlas.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package technology.iatlas.spaceup.controller.api

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.config.SpaceupPathConfig
import technology.iatlas.spaceup.core.annotations.Installed
import technology.iatlas.spaceup.dto.*
import technology.iatlas.spaceup.services.ServiceService
import java.util.*

@Installed
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/service")
class ServiceController(private val serviceService: ServiceService,
                        private val config: SpaceupPathConfig) {
    private val log = LoggerFactory.getLogger(ServiceController::class.java)

    /**
     * Execute an operation change to a Service
     */
    @Post("/execute/{servicename}/{option}")
    suspend fun execute(servicename: String, option: ServiceOption): Feedback {
        log.debug("Execute $servicename: $option")

        val serviceName = Service(servicename, null, null)
        return serviceService.execute(serviceName, option)
    }

    /**
     * Get all services with their statuses
     */
    @Get("/list", produces = [MediaType.APPLICATION_JSON])
    suspend fun list(): List<Service> {
        return serviceService.list()
    }

    @Delete("/delete/{servicename}")
    suspend fun deleteService(servicename: String): Feedback {
        log.warn("Delete service: $servicename")
        return serviceService.deleteService(servicename, config.services)
    }

    /**
     * Gather the info and error logs of a service
     *
     * @param servicename the name of the service we want to get logs
     * @param limit default -1 (for the whole log file)
     * @param reversed default true
     */
    @Get(uri = "/logs/{servicename}{?type}{?limit}{?reversed}", produces = [MediaType.APPLICATION_JSON])
    suspend fun getLogs(servicename: String, type: Optional<Logtype>, limit: Optional<Int>, reversed: Optional<Boolean>): Logfile {
        return serviceService.getLogs(servicename, type.orElse(Logtype.Both), limit.orElse(-1), reversed.orElse(true))
    }

}