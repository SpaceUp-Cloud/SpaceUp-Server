package technology.iatlas.spaceup.core.interceptor

import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.security.annotation.Secured
import jakarta.inject.Singleton
import technology.iatlas.spaceup.core.exceptions.AuthenticationException
import technology.iatlas.spaceup.services.SpaceUpService

@Singleton
@InterceptorBean(Secured::class)
class AuthenticatedInterceptor(
    private val spaceUpService: SpaceUpService
): MethodInterceptor<Any, Any> {
    override fun intercept(context: MethodInvocationContext<Any, Any>?): Any? {
        if(spaceUpService.isDevMode()) {
            return context?.proceed()
        } else {
            throw AuthenticationException("You must be authenticated to access this API!")
        }
    }

}