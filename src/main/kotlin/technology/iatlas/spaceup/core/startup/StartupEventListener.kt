package technology.iatlas.spaceup.core.startup

import io.micronaut.context.env.Environment
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.dto.Command
import technology.iatlas.spaceup.services.SshService

@Singleton
class StartupEventListener(
    env: Environment,
    private val sshService: SshService
) {
    private val log = LoggerFactory.getLogger(StartupEventListener::class.java)

    // TODO: 22.12.2021 replace with Void and create dummy parser as it has nothing to parse
    //private val remoteRunner = Runner<String>(env, sshService)

    @EventListener
    internal fun onApplicationEvent(event: StartupEvent) {
        log.info("Running SpaceUp startup")

        // Step 1: create directories if not exist
        createDirectories()

        log.info("Finished SpaceUp startup")
    }

    private fun createDirectories() {
        log.info("Create remote directories")
        val createDirectoryX = mutableListOf("mkdir", "-p", "")

        val remoteHome = "~/.spaceup"
        log.info("Create $remoteHome")
        runBlocking {
            createDirectoryX[2] = remoteHome
            sshService.execute(Command(createDirectoryX))
        }

        val remoteScriptDir = "~/.spaceup/tmp"
        log.info("Create $remoteScriptDir")
        runBlocking {
            createDirectoryX[2] = remoteScriptDir
            sshService.execute(Command(createDirectoryX))
        }
    }
}