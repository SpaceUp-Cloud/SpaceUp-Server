package technology.iatlas.spaceup.core.interceptor

import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import jakarta.inject.Singleton
import technology.iatlas.spaceup.core.annotations.Installation
import technology.iatlas.spaceup.dto.Server
import technology.iatlas.spaceup.services.DbService

@Singleton
@InterceptorBean(Installation::class)
class InstallationInterceptor(
    private val dbService: DbService
) : MethodInterceptor<Any, Any> {

    override fun intercept(context: MethodInvocationContext<Any, Any>?): Any? {
        val db = dbService.getDb()
        val serverRepo = db.getRepository(Server::class.java)

        val server = serverRepo.find().first()
        if(server.installed) {
            /*throw InstalledException("App is already installed. " +
                    "Remove spaceup.db and restart Server if you need to.")*/

            return HttpResponse.status<String>(HttpStatus.EXPECTATION_FAILED, "App is already installed!")
        }

        return context?.proceed()
    }
}