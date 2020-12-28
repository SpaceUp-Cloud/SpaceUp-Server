package technology.iatlas.spaceup.services

import io.micronaut.cache.annotation.Cacheable
import io.micronaut.http.annotation.Controller
import org.reflections.Reflections
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.config.DevUrlConfig
import technology.iatlas.spaceup.core.annotations.WebNavigation
import technology.iatlas.spaceup.dto.Navigation
import javax.inject.Named
import javax.inject.Singleton

@Singleton
open class NavigationService(
    @Named("swagger") swaggerConfig: DevUrlConfig
) {
    private val log = LoggerFactory.getLogger(NavigationService::class.java)

    private val configList = mutableListOf<DevUrlConfig>()

    init {
        configList.add(swaggerConfig)
    }

    @Cacheable("webnavigation")
    open fun getNavigations(): List<Navigation> {
        val listOfWebNavigation: MutableList<Navigation> = mutableListOf()

        val reflections = Reflections("technology.iatlas.spaceup")
        val annotatedClass = reflections.getTypesAnnotatedWith(Controller::class.java)

        annotatedClass.toList().forEach { classes ->
            classes.methods.filter { m ->
                m.isAnnotationPresent(WebNavigation::class.java)
            }.forEach {
                val nav = it.getAnnotation(WebNavigation::class.java)
                val navigation = Navigation(nav.linkname, nav.mapping, nav.prio, nav.newTab)
                listOfWebNavigation.add(navigation)
            }
        }

        var priorityDevCounter = 100

        configList.forEach {
            if(it.enabled!!) {
                val linkname = it.name.replace(it.name[0], it.name[0].toUpperCase())
                val navigation = Navigation(linkname, it.mapping!!, priorityDevCounter, it.newtab!!)
                listOfWebNavigation.add(navigation)

                priorityDevCounter++
            }
        }

        // Sortiere die Liste nach Prio
        listOfWebNavigation.sortBy {
            it.prio
        }
        log.trace("Navigations found: {}", listOfWebNavigation)

        return listOfWebNavigation
    }

}