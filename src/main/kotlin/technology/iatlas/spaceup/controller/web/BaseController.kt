package technology.iatlas.spaceup.controller.web

import technology.iatlas.spaceup.services.RouterService

abstract class BaseController(routerService: RouterService) {
    /*
        Map controller routes & link names for client
     */
    var mapLinks: Map<String, String> = hashMapOf()

    init {
        mapLinks = routerService.createControllerNameLink()
    }
}