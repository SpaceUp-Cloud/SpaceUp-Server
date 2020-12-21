package technology.iatlas.spaceup.services

import io.micronaut.cache.annotation.Cacheable
import io.micronaut.context.env.Environment
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.tracing.annotation.ContinueSpan
import org.reflections.Reflections
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.annotations.ClientLink
import javax.inject.Singleton


@Singleton
open class ClassService(private val env: Environment) {

    private val log = LoggerFactory.getLogger(ClassService::class.java)

    /**
     * Linking between controller path and navigation name
     */
    @Cacheable("client-controller-link")
    @ContinueSpan
    open fun createControllerNameLink(): MutableMap<String, String> {
        val mapping = mutableMapOf<String, String>()

        getControllerClasses(Controller::class.java).filter {
            it.isAnnotationPresent(Controller::class.java) &&
                    it.methods.any { m ->
                        m.isAnnotationPresent(ClientLink::class.java) &&
                                m.isAnnotationPresent(Get::class.java)
                    }
        }.forEach {
            val controller = it.getAnnotation(Controller::class.java)
            val controllerUri = controller.value

            // Iterate over "Get" annotations
            val methods =
                it.methods.toList()

            methods.filter { method ->
                method.isAnnotationPresent(ClientLink::class.java) &&
                        method.isAnnotationPresent(Get::class.java)
            }.forEach { method ->
                // Check if method has necessary annotations
                val getUri = method.getAnnotation(Get::class.java)
                val clientLink = method.getAnnotation(ClientLink::class.java)
                val clientLinkName = clientLink.name

                if (getUri.uri.isNotEmpty() && getUri.uri != "/") {
                    /*
                        Case:
                        - Getter has Uri
                    */
                    val actualUri = controllerUri + getUri.uri
                    log.debug("Found Link: {} to {}", clientLink, actualUri)
                    mapping[clientLinkName] = actualUri
                } else {
                    /*
                        Case:
                        - Getter does not have Uri
                    */
                    log.debug("Found Link: {} to {}", clientLink, controllerUri)
                    mapping[clientLinkName] = controllerUri
                }
            }
        }
        return mapping
    }

    private fun getControllerClasses(
        annotation: Class<out Annotation>,
        givenPackage: String = "technology.iatlas.spaceup"
    ): List<Class<*>> {
        val reflections = Reflections(givenPackage)
        val annotatedClass = reflections.getTypesAnnotatedWith(annotation)

        return annotatedClass.toList()
    }
}