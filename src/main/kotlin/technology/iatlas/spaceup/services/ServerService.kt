package technology.iatlas.spaceup.services

import io.micronaut.context.env.Environment
import technology.iatlas.spaceup.core.cmd.Command
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.core.parser.EchoParser
import technology.iatlas.spaceup.core.parser.QuotaParser
import technology.iatlas.spaceup.dto.Disk
import javax.inject.Singleton

@Singleton
class ServerService(private val env: Environment) {

    fun getDiskQuota(): Disk? {
        val cmd = mutableListOf("quota", "-gsl")
        return Runner<Disk?>(env).execute(Command(cmd), QuotaParser())
    }

    fun getHostname(): String? {
        val cmd = mutableListOf("hostname")
        return Runner<String>(env).execute(Command(cmd), EchoParser())
    }
}