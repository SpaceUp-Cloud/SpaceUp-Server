/*
 * Copyright (c) 2023 Thraax Session <spaceup@iatlas.technology>.
 *
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

package technology.iatlas.spaceup.core.auth

import io.micronaut.core.annotation.Nullable
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.scheduling.annotation.Scheduled
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.FlowableEmitter
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.litote.kmongo.reactivestreams.getCollection
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import technology.iatlas.spaceup.core.annotations.Installed
import technology.iatlas.spaceup.core.helper.colored
import technology.iatlas.spaceup.dto.IpApi
import technology.iatlas.spaceup.dto.db.User
import technology.iatlas.spaceup.services.DbService
import technology.iatlas.spaceup.services.SecurityService
import technology.iatlas.spaceup.services.SpaceUpService

@Installed
@Singleton
class AuthenticationProviderUserPassword<T>(
    private val dbService: DbService,
    private val securityService: SecurityService,
    private val httpClient: HttpClient,
    private val spaceUpService: SpaceUpService
) : AuthenticationProvider<T> {
    private val log = LoggerFactory.getLogger(AuthenticationProviderUserPassword::class.java)
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO

    val iplist = mutableListOf<IpRequest>()

    override fun authenticate(
        @Nullable httpRequest: @Nullable T,
        authenticationRequest: AuthenticationRequest<*, *>?
    ): Publisher<AuthenticationResponse>? {
        val authPublisher = Flowable.create({ emitter: FlowableEmitter<AuthenticationResponse> ->
            runBlocking {
                if (httpRequest != null) {
                    val incomingRequest = httpRequest as HttpRequest<*>
                    val requestIp =
                        incomingRequest.headers["X-Forwarded-For"] ?: incomingRequest.remoteAddress.address.hostAddress

                    var localGeoIp: IpRequest? = iplist.find { it.ip == requestIp }

                    log.debug("Cache: {}", iplist)
                    // Check if request is cached, else we need to validated IP address
                    localGeoIp = if (localGeoIp == null) {
                        log.info("Validating IP address $requestIp")
                        withContext(dispatcher) {
                            validateIp(httpRequest).asFlow().first()
                        }
                    } else {
                        // We already validated this IP address, so we can use the cached version
                        log.info("Using cached IP address $requestIp")
                        iplist.find { it.ip == requestIp }!!
                    }

                    // Check if request is from Germany
                    if (localGeoIp!!.countryCode == "DE") {
                        val response = authenticateUser(authenticationRequest).asFlow().first()
                        if (response.isAuthenticated) {
                            log.info(
                                "Authenticated user ${authenticationRequest?.identity} " +
                                        "from country ${localGeoIp!!.countryCode} with ip $requestIp."
                            )
                        }
                        emitter.onNext(response)
                        emitter.onComplete()
                    } else {
                        log.warn("Blocked authentication from country ${localGeoIp.countryCode} with ip $requestIp.")
                        emitter.onError(AuthenticationResponse.exception())
                    }
                }
            }
        }, BackpressureStrategy.LATEST)
        return authPublisher
    }

    private fun authenticateUser(
        authenticationRequest: AuthenticationRequest<*, *>?
    ): Flowable<AuthenticationResponse> {
        return Flowable.create({ emitter: FlowableEmitter<AuthenticationResponse> ->
            if (authenticationRequest != null) {
                val db = dbService.getDb()
                val userRepo = db.getCollection<User>()

                runBlocking {
                    val userFound = userRepo.find().asFlow().toList().find {
                        var found = false

                        securityService.decrypt(it) {
                            found = (authenticationRequest.identity == it.username &&
                                    authenticationRequest.secret == it.password)
                        }
                        found
                    }

                    if (userFound != null) {
                        emitter.onNext(AuthenticationResponse.success(userFound.username))
                        emitter.onComplete()
                    } else {
                        colored {
                            log.error("User ${authenticationRequest.identity} or password is not correct!".red.bold)
                        }
                        emitter.onError(AuthenticationResponse.exception())
                        emitter.onComplete()
                    }
                }
            }
        }, BackpressureStrategy.ERROR)
    }

    suspend fun validateIp(httpRequest: T): Mono<IpRequest> {
        val incomingRequest = httpRequest as HttpRequest<*>
        val ip = incomingRequest.headers["X-Forwarded-For"] ?: incomingRequest.remoteAddress.address.hostAddress
        log.debug("Possible authentication from {} with headers {}", ip, incomingRequest.headers.asMap())

        val ipRequest = IpRequest("", "")
        ipRequest.ip = ip

        // Make it work for local development / requests
        return if (ip == "localhost" || ip == "127.0.0.1" || ip == "0:0:0:0:0:0:0:1" || spaceUpService.isDevMode()) {
            ipRequest.countryCode = "DE"
            iplist.add(ipRequest)
            Mono.just(
                ipRequest
            )

        } else {
            val request: HttpRequest<*> =
                // TODO for future: Move API url to configuration
                HttpRequest.GET<Any>("http://ip-api.com/json/$ip")
            val response = httpClient.retrieve(request, Argument.of(IpApi::class.java))
            ipRequest.countryCode = response.asFlow().first().countryCode
            iplist.add(ipRequest)
            Mono.just(
                ipRequest
            )
        }
    }

    @Scheduled(fixedDelay = "1h")
    fun clearCacheScheduler() {
        log.info("Clearing IP cache")
        iplist.clear()
    }
}

data class IpRequest(
    var ip: String,
    var countryCode: String
)