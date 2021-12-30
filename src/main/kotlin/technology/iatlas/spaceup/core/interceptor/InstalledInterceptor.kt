package technology.iatlas.spaceup.core.interceptor

import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import jakarta.inject.Singleton
import technology.iatlas.spaceup.core.annotations.Installed
import technology.iatlas.spaceup.core.exceptions.NotInstalledException
import technology.iatlas.spaceup.dto.Server
import technology.iatlas.spaceup.services.DbService

@Singleton
@InterceptorBean(Installed::class)
class InstalledInterceptor(
    private val dbService: DbService
) : MethodInterceptor<Any, Any> {

    override fun intercept(context: MethodInvocationContext<Any, Any>?): Any? {
        val db = dbService.getDb()
        val serverRepo = db.getRepository(Server::class.java)

        val server = serverRepo.find().first()
        if(!server.installed) {
            throw NotInstalledException("Application needs to be proper installed!")
        }

        return context?.proceed()
    }
}