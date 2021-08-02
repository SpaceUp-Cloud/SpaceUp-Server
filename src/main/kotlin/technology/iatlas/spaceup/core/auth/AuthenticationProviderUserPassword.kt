package technology.iatlas.spaceup.core.auth

import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.*
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import org.reactivestreams.Publisher
import technology.iatlas.spaceup.config.SpaceUpSshConfig
import javax.inject.Singleton

@Singleton
class AuthenticationProviderUserPassword(
    private val sshConfig: SpaceUpSshConfig
): AuthenticationProvider  {
    override fun authenticate(
        httpRequest: HttpRequest<*>?,
        authenticationRequest: AuthenticationRequest<*, *>?
    ): Publisher<AuthenticationResponse> {
        return Flowable.create({
            emitter: FlowableEmitter<AuthenticationResponse> ->
            if (authenticationRequest != null) {
                // Authenticate locally
                if(authenticationRequest.identity == sshConfig.username && authenticationRequest.secret == sshConfig.password) {
                    emitter.onNext(UserDetails(authenticationRequest.identity as String, ArrayList()))
                    emitter.onComplete()
                } else {
                    emitter.onError(AuthenticationException(AuthenticationFailed()))
                }
            } else {
                emitter.onError(AuthenticationException(AuthenticationFailed()))
            }
        }, BackpressureStrategy.ERROR)
    }

}