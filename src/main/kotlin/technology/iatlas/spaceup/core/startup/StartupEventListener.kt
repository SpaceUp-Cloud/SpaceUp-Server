/*
 * Copyright (c) 2022 Gino Atlas.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package technology.iatlas.spaceup.core.startup

import com.lordcodes.turtle.ShellLocation
import com.lordcodes.turtle.shellRun
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.getCollection
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.cmd.toFeedback
import technology.iatlas.spaceup.core.helper.colored
import technology.iatlas.spaceup.dto.Command
import technology.iatlas.spaceup.dto.db.Server
import technology.iatlas.spaceup.isOk
import technology.iatlas.spaceup.services.*
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

@Singleton
class StartupEventListener(
    private val dbService: DbService,
    private val installerService: InstallerService,
    private val spaceUpService: SpaceUpService,
    private val swsService: SwsService,
    private val sshService: SshService,
) {
    private val log = LoggerFactory.getLogger(StartupEventListener::class.java)

    private val os = System.getProperty("os.name")

    @EventListener
    internal fun onApplicationEvent(event: StartupEvent) {
        showBanner()
        log.info("Running SpaceUp startup")
        log.debug("OS: $os")

        if(spaceUpService.isDevMode()) {
            colored {
                log.warn("""
                    You are running in DEV mode!
                    If property 'spaceup.dev.ssh.db-credentials' is set to false
                    then supply all necessary SSH configuration as parameters to ensure SpaceUp can run as expected!"""
                    .yellow.bold.trimIndent())
            }
        }

        // Step 1: create directories if not exist
        createDirectories()
        runBlocking {
            createExternalDirectories()
        }

        // Step 2: check if spaceup was installed
        checkInstallation()

        // Step 3: fill sws cache
        //fillSwsCache()

        log.info("Finished SpaceUp startup")
    }

    private fun createDirectories() {
        log.info("Create local directories")

        val spaceupHome = ".spaceup"
        val spaceupTempDir = ".spaceup/tmp"

        if(os.lowercase().contains(Regex("(linux|mac)"))) {
            val home = ShellLocation.HOME

            shellRun(home) {
                log.info("Create $home/$spaceupHome")
                command("mkdir", listOf("-p", spaceupHome))
                log.info("Create $home/$spaceupTempDir")
                command("mkdir", listOf("-p", spaceupTempDir))
            }
            // Set properties for spaceup
            System.setProperty("spaceup.home", "$home/$spaceupHome")
            System.setProperty("spaceup.tempdir", "$home/$spaceupTempDir")
        } else if (os.lowercase().contains("windows")) {
            val home = System.getProperty("user.home")

            log.info("Create $home\\$spaceupHome")
            Path("$home/$spaceupHome").normalize().createDirectories()
            System.setProperty("spaceup.home", "$home/$spaceupHome")
            log.info("Create $home\\${spaceupTempDir.createNormalizedPath()}")
            Path("$home/$spaceupTempDir").normalize().createDirectories()
            System.setProperty("spaceup.tempdir", "$home/$spaceupTempDir")
        }
    }

    private suspend fun createExternalDirectories() {
        log.info("Create external directories")
        val cmd = Command(mutableListOf(
            "mkdir", "-p", "~/.spaceup",
            "mkdir", "-p", "~/.spaceup/tmp"
        ))
        val response = sshService.execute(cmd)
        val feedback = response.toFeedback()
        if(!feedback.isOk()) {
            log.error(feedback.error)
        }
    }

    private fun checkInstallation() {
        // Let's check if we are already installed properly
        val db = dbService.getDb()
        val serverRepo = db.getCollection<Server>()
        val server = serverRepo.find().firstOrNull()

        if(server == null) {
            log.info("Seems to be first run. Set not installed!")
            val apiKey = installerService.generateAPIKey()
            colored {
                log.info("Finish installation with API key: ${apiKey.yellow.bold}")
            }
            val doc = Server(false, apiKey)
            serverRepo.insertOne(doc)
        } else {
            val installed = server.installed
            if(!installed) {
                colored {
                    log.info("Finish installation with API key: ${server.apiKey.yellow.bold}")
                }
            }
        }
    }

    private fun showBanner() {
        colored {
            println("""   ▄████████    ▄███████▄    ▄████████  ▄████████    ▄████████ ███    █▄     ▄███████▄ 
  ███    ███   ███    ███   ███    ███ ███    ███   ███    ███ ███    ███   ███    ███ 
  ███    █▀    ███    ███   ███    ███ ███    █▀    ███    █▀  ███    ███   ███    ███ 
  ███          ███    ███   ███    ███ ███         ▄███▄▄▄     ███    ███   ███    ███ 
▀███████████ ▀█████████▀  ▀███████████ ███        ▀▀███▀▀▀     ███    ███ ▀█████████▀  
         ███   ███          ███    ███ ███    █▄    ███    █▄  ███    ███   ███        
   ▄█    ███   ███          ███    ███ ███    ███   ███    ███ ███    ███   ███        
 ▄████████▀   ▄████▀        ███    █▀  ████████▀    ██████████ ████████▀   ▄████▀      """.cyan.bold.trimIndent())
        }
        colored {
            println("\tSpaceUp Server (${spaceUpService.getSpaceUpVersion()})".cyan.bold)
        }
    }
}

fun String.createNormalizedPath(): Path {
    return Path(this).normalize()
}