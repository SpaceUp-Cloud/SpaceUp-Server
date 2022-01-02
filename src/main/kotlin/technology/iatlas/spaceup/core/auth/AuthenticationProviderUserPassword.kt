/*
 * Copyright (c) 2022 Gino Atlas.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package technology.iatlas.spaceup.core.auth

import io.micronaut.context.annotation.Context
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.FlowableEmitter
import org.reactivestreams.Publisher
import technology.iatlas.spaceup.config.SpaceUpSshConfig

@Context
class AuthenticationProviderUserPassword(
    private val sshConfig: SpaceUpSshConfig
): AuthenticationProvider  {
    override fun authenticate(
        httpRequest: HttpRequest<*>?,
        authenticationRequest: AuthenticationRequest<*, *>?
    ): Publisher<AuthenticationResponse>? {
        return Flowable.create({
            emitter: FlowableEmitter<AuthenticationResponse> ->
            if (authenticationRequest != null) {
                // Authenticate locally
                if(authenticationRequest.identity == sshConfig.username && authenticationRequest.secret == sshConfig.password) {
                    emitter.onNext(AuthenticationResponse.success(sshConfig.username!!))
                    emitter.onComplete()
                } else {
                    emitter.onError(AuthenticationResponse.exception())
                }
            } else {
                emitter.onError(AuthenticationResponse.exception())
            }
        }, BackpressureStrategy.ERROR)
    }
}