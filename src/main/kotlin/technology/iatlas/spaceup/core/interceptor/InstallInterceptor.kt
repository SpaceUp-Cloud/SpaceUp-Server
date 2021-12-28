package technology.iatlas.spaceup.core.interceptor

import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.annotations.Installed
import technology.iatlas.spaceup.services.DbService

@Singleton
@InterceptorBean(Installed::class)
class InstallInterceptor(
    private val dbService: DbService
) : MethodInterceptor<Any, Any> {

    private val log = LoggerFactory.getLogger(InstallInterceptor::class.java)

    override fun intercept(context: MethodInvocationContext<Any, Any>?): Any? {
        val db = dbService.getDb()
        val serverCollection = db.getCollection("server")

        val doc = serverCollection.find()
        log.debug("Server collection size: ${doc.size()}")
        doc.forEach {
            log.debug(it.toString())
            if(it["installed"] == false) {
                throw Exception("Not installed!")
            }
        }

        return context?.proceed()
    }
}