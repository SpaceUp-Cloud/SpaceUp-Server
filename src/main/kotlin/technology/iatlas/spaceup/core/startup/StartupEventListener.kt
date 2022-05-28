/*
 * Copyright (c) 2022 spaceup@iatlas.technology.
 * SpaceUp-Server is free software; You can redistribute it and/or modify it under the terms of:
 *   - the GNU Affero General Public License version 3 as published by the Free Software Foundation.
 * You don't have to do anything special to accept the license and you don’t have to notify anyone which that you have made that decision.
 *
 * SpaceUp-Server is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See your chosen license for more details.
 *
 * You should have received a copy of both licenses along with SpaceUp-Server
 * If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * There is a strong belief within us that the license we have chosen provides not only the best solution for providing you with the essential freedom necessary to use SpaceUp-Server within your projects, but also for maintaining enough copyleft strength for us to feel confident and secure with releasing our hard work to the public. For your convenience we've included our own interpretation of the license we chose, which can be seen below.
 *
 * Our interpretation of the GNU Affero General Public License version 3: (Quoted words are words in which there exists a definition within the license to avoid ambiguity.)
 *   1. You must always provide the source code, copyright and license information of SpaceUp-Server whenever you "convey" any part of SpaceUp-Server;
 *      be it a verbatim copy or a modified copy.
 *   2. SpaceUp-Server was developed as a library and has therefore been designed without knowledge of your work; as such the following should be implied:
 *      a) SpaceUp-Server was developed without knowledge of your work; as such the following should be implied:
 *         i)  SpaceUp-Server should not fall under a work which is "based on" your work.
 *         ii) You should be free to use SpaceUp-Server in a work covered by the:
 *             - GNU General Public License version 2
 *             - GNU Lesser General Public License version 2.1
 *             This is due to those licenses classifying SpaceUp-Server as a work which would fall under an "aggregate" work by their terms and definitions;
 *             as such it should not be covered by their terms and conditions. The relevant passages start at:
 *             - Line 129 of the GNU General Public License version 2
 *             - Line 206 of the GNU Lesser General Public License version 2.1
 *      b) If you have not "modified", "adapted" or "extended" SpaceUp-Server then your work should not be bound by this license,
 *         as you are using SpaceUp-Server under the definition of an "aggregate" work.
 *      c) If you have "modified", "adapted" or "extended" SpaceUp-Server then any of those modifications/extensions/adaptations which you have made
 *         should indeed be bound by this license, as you are using SpaceUp-Server under the definition of a "based on" work.
 *
 * Our hopes is that our own interpretation of license aligns perfectly with your own values and goals for using our work freely and securely. If you have any questions at all about the licensing chosen for SpaceUp-Server you can email us directly at spaceup@iatlas.technology or you can get in touch with the license authors (the Free Software Foundation) at licensing@fsf.org to gain their opinion too.
 *
 * Alternatively you can provide feedback and acquire the support you need at our support forum. We'll definitely try and help you as soon as possible, and to the best of our ability; as we understand that user experience is everything, so we want to make you as happy as possible! So feel free to get in touch via our support forum and chat with other users of SpaceUp-Server here at:
 * https://spaceup.iatlas.technology
 *
 * Thanks, and we hope you enjoy using SpaceUp-Server and that it's everything you ever hoped it could be.
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
            log.warn("Currently only GNU/Linux is supported!")
            return
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