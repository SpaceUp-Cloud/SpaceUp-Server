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
import org.litote.kmongo.getCollection
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.helper.colored
import technology.iatlas.spaceup.dto.db.Server
import technology.iatlas.spaceup.services.DbService
import technology.iatlas.spaceup.services.InstallerService
import technology.iatlas.spaceup.services.SpaceUpService
import technology.iatlas.spaceup.services.SystemService
import kotlin.system.exitProcess

@Singleton
class StartupEventListener(
    private val dbService: DbService,
    private val installerService: InstallerService,
    private val systemService: SystemService,
    private val spaceUpService: SpaceUpService
) {
    private val log = LoggerFactory.getLogger(StartupEventListener::class.java)

    @EventListener
    internal fun onApplicationEvent(event: StartupEvent) {
        showBanner()
        log.info("Running SpaceUp startup")

        val os = System.getProperty("os.name")
        log.debug("OS: $os")
        if(!os.lowercase().contains("linux")) {
            colored {
                log.warn("Currently only GNU/Linux is supported!")
                log.warn("Will shutdown server now.".red.bold)
            }
            exitProcess(2)
        }

        if(spaceUpService.isDevMode()) {
            colored {
                log.warn("You are running in DEV mode!".yellow.bold)
                log.warn("If 'spaceup.dev.ssh.db-credentials' set to false".yellow.bold)
                log.warn(
                    "Supply all necessary SSH configuration as parameters to ensure SpaceUp can run as expected!"
                        .yellow.bold)
            }
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
        dbService.init()

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
            println("\tSpaceUp Server (${systemService.getSpaceUpVersion()})".cyan.bold)
        }
    }
}