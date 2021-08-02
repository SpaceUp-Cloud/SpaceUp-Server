package technology.iatlas.spaceup.controller.web

import com.fizzed.rocker.BindableRockerModel
import com.fizzed.rocker.Rocker
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.services.NavigationService
import javax.inject.Inject

@Secured(SecurityRule.IS_AUTHENTICATED)
abstract class BaseController {
    private val log = LoggerFactory.getLogger(BaseController::class.java)

    @Inject
    private lateinit var navigationService: NavigationService

    fun getRockerTemplate(templateName: String, vararg args: String): BindableRockerModel {
        val navigationList = navigationService.getNavigations()

        return Rocker.template(templateName, args)
            .bind("mapLinks", navigationList)
    }

    fun getRockerTemplate(templateName: String): BindableRockerModel {
        val navigationList = navigationService.getNavigations()

        return Rocker.template(templateName)
            .bind("navigations", navigationList)
    }
}