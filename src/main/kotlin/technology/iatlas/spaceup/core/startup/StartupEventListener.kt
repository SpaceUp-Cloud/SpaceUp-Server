package technology.iatlas.spaceup.core.startup

import com.lordcodes.turtle.ShellLocation
import com.lordcodes.turtle.shellRun
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.dto.Server
import technology.iatlas.spaceup.services.DbService

@Singleton
class StartupEventListener(
    private val dbService: DbService
) {
    private val log = LoggerFactory.getLogger(StartupEventListener::class.java)

    @EventListener
    internal fun onApplicationEvent(event: StartupEvent) {
        log.info("Running SpaceUp startup")

        val os = System.getProperty("os.name")
        log.debug("OS: $os")
        if(!os.lowercase().contains("linux")) {
            log.warn("Currently only GNU/Linux is supported!")
            return
        }

        // Step 1: create directories if not exist
        createDirectories()

        // Step 2: init and migrate db
        initDb()

        log.info("Finished SpaceUp startup")
    }

    private fun createDirectories() {
        log.info("Create remote directories")

        val home = ShellLocation.HOME
        val remoteHome = ".spaceup"
        val remoteScriptDir = ".spaceup/tmp"

        shellRun(home) {
            log.info("Create $home/$remoteHome")
            command("mkdir", listOf("-p", remoteHome))
            log.info("Create $home/$remoteScriptDir")
            command("mkdir", listOf("-p", remoteScriptDir))
        }
    }

    private fun initDb() {
        dbService.initDb()

        val db = dbService.getDb()
        val serverRepo = db.getRepository(Server::class.java)
        val server = serverRepo.find()

        if(server?.firstOrNull() == null) {
            log.info("Seems to be first run. Set not installed!")
            val doc = Server(false)
            serverRepo.insert(doc)
        } else {
            log.info("Installed: ${server.first()}")
        }
    }
}