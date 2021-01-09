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
    private val env: Environment,
    private val sshService: SshService
    ) {

    suspend fun getDiskQuota(): Disk? {
        val cmd = mutableListOf("quota", "-gsl")
        val runner = Runner<Disk?>(env, sshService)
        runner.execute(Command(cmd), QuotaParser())

        var disk: Disk? = Disk("", 0f, "", 0f)
        runner.getBehaviourSubject().subscribe {
            disk = it
        }

        return disk
    }

    suspend fun getHostname(): Hostname {
        val cmd = mutableListOf("hostname")
        val runner = Runner<String>(env, sshService)
        runner.execute(Command(cmd), EchoParser())

        lateinit var hostname: Hostname
        runner.getBehaviourSubject().subscribe {
            hostname = Hostname(it.trim())
        }

        return hostname
    }
}