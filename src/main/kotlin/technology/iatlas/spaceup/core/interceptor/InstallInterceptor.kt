package technology.iatlas.spaceup.core.interceptor

import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import jakarta.inject.Singleton
import org.dizitart.no2.filters.Filter
import technology.iatlas.spaceup.core.annotations.Installed
import technology.iatlas.spaceup.core.exceptions.NotInstalledException
import technology.iatlas.spaceup.services.DbService

@Singleton
@InterceptorBean(Installed::class)
class InstallInterceptor(
    private val dbService: DbService
) : MethodInterceptor<Any, Any> {

    override fun intercept(context: MethodInvocationContext<Any, Any>?): Any? {
        val db = dbService.getDb()
        val serverCollection = db.getCollection("server")

        val doc = serverCollection.find(Filter.ALL)
        val installed = doc.first().get("installed")
        if(installed == false) {
            throw NotInstalledException("App is not properly installed! Run installer first.")
        }

        return context?.proceed()
    }
}