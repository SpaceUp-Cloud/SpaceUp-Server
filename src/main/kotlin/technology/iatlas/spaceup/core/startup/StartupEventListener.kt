package technology.iatlas.spaceup.core.startup

import com.lordcodes.turtle.ShellLocation
import com.lordcodes.turtle.shellRun
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton
import org.dizitart.no2.migration.Instructions
import org.dizitart.no2.migration.Migration
import org.slf4j.LoggerFactory
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
            log.warn("Currently only GNU/Linux is supported for local setup!")
            return
        }

        // Step 1: create directories if not exist
        createDirectories()

        log.info("Finished SpaceUp startup")
    }

    private fun createDirectories() {
        log.info("Create remote directories")

        val home = ShellLocation.HOME
        val remoteHome = ".spaceup"
        val remoteScriptDir = ".spaceup/tmp"

        shellRun(home) {
            log.info("Create $remoteHome")
            command("mkdir", listOf("-p", remoteHome))
            log.info("Create $remoteScriptDir")
            command("mkdir", listOf("-p", remoteScriptDir))
        }
    }

    private fun createDbAndInit() {
        val db = dbService.createOrOpen()

        val migration1 = Migration1(0, 1)
        db.schemaVersion(0).addMigrations(migration1)
            .openOrCreate("SpaceUp", "Spac3Up!***REMOVED***")

    }
}

class Migration1(startVersion: Int, endVersion: Int) : Migration(startVersion, endVersion) {
    override fun migrate(instructions: Instructions?) {
        instructions
            ?.forRepository("user")
            ?.addField<String>("username")
            ?.addField<String>("password")

        instructions
            ?.forRepository("ssh")
            ?.addField<String>("username")
            ?.addField<String>("password")

        instructions
            ?.forRepository("auth")
            ?.addField<String>("jwtTime", "60")
    }

}