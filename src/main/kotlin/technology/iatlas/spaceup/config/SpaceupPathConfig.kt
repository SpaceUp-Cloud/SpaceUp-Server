package technology.iatlas.spaceup.config

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("spaceup.path")
class SpaceupPathConfig {
    var services: String = "~/etc/services.d"
}