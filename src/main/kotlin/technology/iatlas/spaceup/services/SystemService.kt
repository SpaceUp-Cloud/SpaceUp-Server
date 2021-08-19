package technology.iatlas.spaceup.services

import io.micronaut.context.env.Environment
import technology.iatlas.spaceup.dto.Command
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.core.parser.EchoParser
import technology.iatlas.spaceup.core.parser.QuotaParser
import technology.iatlas.spaceup.dto.Disk
import technology.iatlas.spaceup.dto.Hostname
import javax.inject.Singleton

@Singleton
class SystemService(
    env: Environment,
    sshService: SshService) {
    private val hostnameRunner = Runner<String>(env, sshService)
    private val diskRunner = Runner<Disk>(env, sshService)

    suspend fun getDiskQuota(): Disk {
        val cmd = mutableListOf("quota", "-gsl")
        val emptyQuotaSpace: Float = 0.0F
        var disk: Disk = Disk(
            space = "",
            spacePercentage = emptyQuotaSpace,
            quota = "",
            availableQuota = emptyQuotaSpace)

        diskRunner.subject().subscribe {
            disk = it
        }

        diskRunner.execute(Command(cmd), QuotaParser())
        return disk
    }

    suspend fun getHostname(): Hostname {
        val cmd = mutableListOf("hostname")
        var hostname: Hostname = Hostname("")

        hostnameRunner.subject().subscribe {
            hostname = Hostname(it)
        }

        hostnameRunner.execute(Command(cmd), EchoParser())
        return hostname
    }
}