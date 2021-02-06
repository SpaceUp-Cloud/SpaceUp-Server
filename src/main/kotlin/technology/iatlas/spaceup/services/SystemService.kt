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

    private lateinit var hostname: Hostname
    private val hostnameRunner = Runner<String>(env, sshService)

    private lateinit var disk: Disk
    private val diskRunner = Runner<Disk>(env, sshService)

    init {
        diskRunner.subject().subscribe {
            disk = it
        }

        hostnameRunner.subject().subscribe {
            hostname = Hostname(it)
        }
    }

    suspend fun getDiskQuota(): Disk? {
        val cmd = mutableListOf("quota", "-gsl")
        diskRunner.execute(Command(cmd), QuotaParser())
        return disk
    }

    suspend fun getHostname(): Hostname {
        val cmd = mutableListOf("hostname")
        hostnameRunner.execute(Command(cmd), EchoParser())

        return hostname
    }
}