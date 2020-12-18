package technology.iatlas.spaceup.controller.web

import com.fizzed.rocker.BindableRockerModel
import com.fizzed.rocker.Rocker
import technology.iatlas.spaceup.services.RouterService

abstract class BaseController(routerService: RouterService) {
    /*
        Map controller routes & link names for client
     */
    var mapLinks: Map<String, String> = hashMapOf()

    init {
        mapLinks = routerService.createControllerNameLink()
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