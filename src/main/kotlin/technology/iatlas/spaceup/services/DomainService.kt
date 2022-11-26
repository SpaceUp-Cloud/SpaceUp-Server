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
 * There is a strong belief within us that the license we have chosen provides not only the best solution for providing you with the essential freedom necessary to use SpaceUp-Server within your projects, but also for maintaining enough copyleft strength for us to feel confident and secure with releasing our hard work to the public. For your convenience we've included our own interpretation of the license we chose, which can be seen below.
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
 *             This is due to those licenses classifying SpaceUp-Server as a work which would fall under an "aggregate" work by their terms and definitions;
 *             as such it should not be covered by their terms and conditions. The relevant passages start at:
 *             - Line 129 of the GNU General Public License version 2
 *             - Line 206 of the GNU Lesser General Public License version 2.1
 *      b) If you have not "modified", "adapted" or "extended" SpaceUp-Server then your work should not be bound by this license,
 *         as you are using SpaceUp-Server under the definition of an "aggregate" work.
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

package technology.iatlas.spaceup.services

import io.micronaut.context.annotation.Context
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.core.parser.CreateDomainParser
import technology.iatlas.spaceup.core.parser.DeleteDomainParser
import technology.iatlas.spaceup.core.parser.DomainParser
import technology.iatlas.spaceup.dto.Command
import technology.iatlas.spaceup.dto.Domain
import technology.iatlas.spaceup.dto.Feedback

@Context
class DomainService(
    private val sshService: SshService,
    private val wsBroadcaster: WsBroadcaster): WsServiceInf {
    private val log = LoggerFactory.getLogger(DomainService::class.java)

    private val domainListRunner = Runner<List<Domain>>(sshService)
    private var domains = listOf<Domain>()

    private val addDomainRunner = Runner<Feedback>(sshService)
    private val deleteDomainRunner = Runner<Feedback>(sshService)

    override val topic = "domains"

    init {
        // Cache domain list for faster access but delay on fresh data
        domainListRunner.subject().subscribe {
            wsBroadcaster.broadcastSync(it, topic)
            domains = it
        }

    }

    suspend fun updateDomainList() {
        val cmd: MutableList<String> = mutableListOf("uberspace", "web", "domain", "list")
        domainListRunner.execute(Command(cmd), DomainParser())
    }

    suspend fun add(domains: List<Domain>): List<Feedback> {
        val feedbacks = linkedSetOf<Feedback>()

        addDomainRunner.subject()
            .distinct()
            .subscribeBy(
                onNext = {
                    feedbacks.add(it)
                    wsBroadcaster.broadcast(it, feedbackTopic)
                },
                onError = { t ->
                    val error = t.message

                    val errorFeedback = Feedback("", "Could not add domain: ")
                    if (error != null) {
                        errorFeedback.error += error
                    }
                    feedbacks.add(errorFeedback)
                    wsBroadcaster.broadcast(errorFeedback, feedbackTopic)
                },
                onComplete = {
                    log.debug("Finished 'Add domain'")
                }
            )

        domains.forEach { domain ->
            // Add domain to cmd
            val cmd: MutableList<String> = mutableListOf("uberspace", "web", "domain", "add")
            cmd.add(domain.url)

            addDomainRunner.execute(Command(cmd), CreateDomainParser(domain.url))
        }

        log.debug("Response: $feedbacks")
        return feedbacks.toMutableList()
    }

    suspend fun delete(domain: Domain): Feedback {
        val cmd = mutableListOf("uberspace", "web", "domain", "del", domain.url)

        var feedback = Feedback("", "")
        deleteDomainRunner.subject()
            .distinct()
            .subscribeBy(
                onNext = {
                    feedback = it
                    wsBroadcaster.broadcast(it, feedbackTopic)
                },
                onError = { t ->
                    // Should not happen
                    log.error(t.message)
                    feedback = t.message?.let { it -> Feedback("", it) }!!
                    wsBroadcaster.broadcast(feedback, feedbackTopic)
                }
            )

        deleteDomainRunner.execute(Command(cmd), DeleteDomainParser(domain.url))
        return feedback
    }

    /**
     * Get all domains
     */
    fun list(): List<Domain> {
        wsBroadcaster.broadcastSync(domains, topic)
        return domains
    }
}