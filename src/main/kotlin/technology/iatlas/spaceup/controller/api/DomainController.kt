/*
 * Copyright (c) 2022 spaceup@iatlas.technology.
 * SpaceUp-Server is free software; You can redistribute it and/or modify it under the terms of:
 *   - the GNU Affero General Public License version 3 as published by the Free Software Foundation.
 * You don't have to do anything special to accept the license and you don’t have to notify anyone which that you have made that decision.
 *
 * SpaceUp-Server is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See your chosen license for more details.
 *
 * You should have received a copy of both licenses along with SpaceUp-Server
 * If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * There is a strong belief within us that the license we have chosen provides not only the best solution for providing you with the essential freedom necessary to use <project name> within your projects, but also for maintaining enough copyleft strength for us to feel confident and secure with releasing our hard work to the public. For your convenience we've included our own interpretation of the license we chose, which can be seen below.
 *
 * Our interpretation of the GNU Affero General Public License version 3: (Quoted words are words in which there exists a definition within the license to avoid ambiguity.)
 *   1. You must always provide the source code, copyright and license information of SpaceUp-Server whenever you "convey" any part of SpaceUp-Server;
 *      be it a verbatim copy or a modified copy.
 *   2. SpaceUp-Server was developed as a library and has therefore been designed without knowledge of your work; as such the following should be implied:
 *      a) SpaceUp-Server was developed without knowledge of your work; as such the following should be implied:
 *         i)  SpaceUp-Server should not fall under a work which is "based on" your work.
 *         ii) You should be free to use SpaceUp-Server in a work covered by the:
 *             - GNU General Public License version 2
 *             - GNU Lesser General Public License version 2.1
 *             This is due to those licenses classifying <project name> as a work which would fall under an "aggregate" work by their terms and definitions;
 *             as such it should not be covered by their terms and conditions. The relevant passages start at:
 *             - Line 129 of the GNU General Public License version 2
 *             - Line 206 of the GNU Lesser General Public License version 2.1
 *      b) If you have not "modified", "adapted" or "extended" SpaceUp-Server then your work should not be bound by this license,
 *         as you are using <project name> under the definition of an "aggregate" work.
 *      c) If you have "modified", "adapted" or "extended" SpaceUp-Server then any of those modifications/extensions/adaptations which you have made
 *         should indeed be bound by this license, as you are using SpaceUp-Server under the definition of a "based on" work.
 *
 * Our hopes is that our own interpretation of license aligns perfectly with your own values and goals for using our work freely and securely. If you have any questions at all about the licensing chosen for SpaceUp-Server you can email us directly at spaceup@iatlas.technology or you can get in touch with the license authors (the Free Software Foundation) at licensing@fsf.org to gain their opinion too.
 *
 * Alternatively you can provide feedback and acquire the support you need at our support forum. We'll definitely try and help you as soon as possible, and to the best of our ability; as we understand that user experience is everything, so we want to make you as happy as possible! So feel free to get in touch via our support forum and chat with other users of SpaceUp-Server here at:
 * https://spaceup.iatlas.technology
 *
 * Thanks, and we hope you enjoy using SpaceUp-Server and that it's everything you ever hoped it could be.
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