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
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import technology.iatlas.spaceup.core.annotations.Installed
import technology.iatlas.spaceup.dto.Disk
import technology.iatlas.spaceup.dto.Hostname
import technology.iatlas.spaceup.services.SystemService

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
        return systemService.getSpaceUpVersion()
    }
}