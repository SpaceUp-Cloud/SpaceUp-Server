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

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.annotations.Installed
import technology.iatlas.spaceup.dto.Domain
import technology.iatlas.spaceup.dto.Feedback
import technology.iatlas.spaceup.services.DomainService
import java.util.*

@Installed
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/domain")
class DomainController(private val domainService: DomainService) {
    private val log = LoggerFactory.getLogger(DomainController::class.java)

    /**
     * Add a new domain
     *
     * @return Map<String, Feedback> - per domain a feedback
     */
    @Post(uri = "/add", produces = [MediaType.APPLICATION_JSON])
    suspend fun add(@Body domains: List<Domain>): HttpResponse<List<Feedback>> {
        log.info("Received list to add: $domains")

        return HttpResponse.ok(domainService.add(domains))
    }

    /**
     * Delete a domain with the corresponded url
     * @return Feedback
     */
    @Delete("/delete/{url}", produces = [MediaType.APPLICATION_JSON])
    suspend fun delete(url: String): HttpResponse<Feedback> {
        val domain = Domain(url)
        log.warn("Delete domain $domain")

        return HttpResponse.ok(domainService.delete(domain))
    }

    /**
     * Get a list of domains
     * @return List<Domain> - list of domains
     */
    @Get(uri = "/list{?cached}", produces = [MediaType.APPLICATION_JSON])
    suspend fun list(cached: Optional<Boolean>): List<Domain> {
        log.debug("Get domain list.")

        if(!get(cached)) {
            log.info("Get updated domain list.")
            domainService.updateDomainList()
        }
        return domainService.list()
    }

    private fun get(myOptional: Optional<Boolean>): Boolean {
        return myOptional.orElse(false)
    }

}