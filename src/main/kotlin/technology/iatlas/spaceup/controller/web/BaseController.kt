package technology.iatlas.spaceup.controller.web

import com.fizzed.rocker.BindableRockerModel
import com.fizzed.rocker.Rocker
import io.micronaut.context.env.Environment
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.config.DevUrlConfig
import technology.iatlas.spaceup.services.ClassService
import javax.inject.Inject
import javax.inject.Named

abstract class BaseController(@Named("swagger") val config: DevUrlConfig,
                              env: Environment, classService: ClassService) {
    private val log = LoggerFactory.getLogger(BaseController::class.java)

    /*
        Map controller routes & link names for client
     */
    var mapLinks: MutableMap<String, String> = mutableMapOf()

    init {
        // TODO: Move this mapping stuff to a new service "WebclientService"
        mapLinks = classService.createControllerNameLink()

        val devUrlEnabled = config.enabled ?: false
        val name = config.name
        val mapping = config.mapping
        val newTab = config.newtab


        if (env.activeNames.contains("dev") && devUrlEnabled) {
            if (mapping != null) mapLinks[name.replace(name[0], name[0].toUpperCase())] = mapping
            log.info("Dev URLs enabled!")
        } else {
            log.info("Dev URLS disabled!")
        }
    }

    fun getRockerTemplate(templateName: String, vararg args: String): BindableRockerModel {
        return Rocker.template(templateName, args)
            .bind("mapLinks", mapLinks)
    }

    fun getRockerTemplate(templateName: String): BindableRockerModel {
        return Rocker.template(templateName)
            .bind("mapLinks", mapLinks)
    }
}