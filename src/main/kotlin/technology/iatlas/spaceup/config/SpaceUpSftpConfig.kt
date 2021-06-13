package technology.iatlas.spaceup.config

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("spaceup.sftp")
class SpaceUpSftpConfig {
    var remotedir: String? = null
}