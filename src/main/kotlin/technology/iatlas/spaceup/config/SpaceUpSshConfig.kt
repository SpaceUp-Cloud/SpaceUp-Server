package technology.iatlas.spaceup.config

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.EachProperty
import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.PropertySource
import io.micronaut.core.annotation.Introspected

@ConfigurationProperties("spaceup.ssh")
class SpaceUpSshConfig {
    var username: String? = null
    var password: String? = null
    var privatekey: String? = null
    var host: String? = null
    var port: Int? = 22
}
