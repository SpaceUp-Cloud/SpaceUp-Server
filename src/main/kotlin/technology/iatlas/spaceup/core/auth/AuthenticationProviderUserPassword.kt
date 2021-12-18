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