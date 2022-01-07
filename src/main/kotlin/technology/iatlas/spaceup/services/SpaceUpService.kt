package technology.iatlas.spaceup.services

import io.micronaut.context.annotation.Context
import io.micronaut.context.env.Environment

@Context
class SpaceUpService(
    private val env: Environment
) {

    fun isDevMode(): Boolean {
        val currentEnv = env.activeNames
        var isDev = false
        currentEnv.forEach {
            if(it.contains("dev")) {
                isDev = true
            }
        }

        return isDev
    }
}