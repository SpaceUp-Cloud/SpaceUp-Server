package technology.iatlas.spaceup.config

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("spaceup.ssh")
class SpaceUpSshConfig {
    var username: String? = null
    var password: String? = null
    var privatekey: String? = null
    var host: String? = null
    var port: Int? = 22
}
