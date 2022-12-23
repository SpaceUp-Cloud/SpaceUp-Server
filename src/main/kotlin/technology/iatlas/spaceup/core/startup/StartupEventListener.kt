/*
 * Copyright (c) 2022 Thraax Session <spaceup@iatlas.technology>.
 *
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

import brave.Tracing
import brave.context.log4j2.ThreadContextScopeDecorator
import brave.propagation.ThreadLocalCurrentTraceContext
import com.lordcodes.turtle.ShellLocation
import com.lordcodes.turtle.shellRun
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.tracing.annotation.NewSpan
import jakarta.inject.Singleton
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import org.litote.kmongo.reactivestreams.getCollection
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.config.SpaceupLocalPathConfig
import technology.iatlas.spaceup.config.SpaceupRemotePathConfig
import technology.iatlas.spaceup.core.cmd.toFeedback
import technology.iatlas.spaceup.core.helper.colored
import technology.iatlas.spaceup.dto.Command
import technology.iatlas.spaceup.dto.db.Server
import technology.iatlas.spaceup.isOk
import technology.iatlas.spaceup.services.*
import technology.iatlas.spaceup.util.createNormalizedPath
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

@Singleton
open class StartupEventListener(
    private val spaceupRemotePathConfig: SpaceupRemotePathConfig,
    private val spaceupLocalPathConfig: SpaceupLocalPathConfig,
    private val installerService: InstallerService,
    private val spaceUpService: SpaceUpService,
    private val swsService: SwsService,
    private val sshService: SshService,
    private val dbService: DbService
): WsServiceInf {
    override val topic: String = "startup"

    private val log = LoggerFactory.getLogger(StartupEventListener::class.java)
    private val os = System.getProperty("os.name")

    @OptIn(DelicateCoroutinesApi::class)
    @EventListener
    internal fun onApplicationEvent(event: StartupEvent) {
        Tracing.newBuilder()
            .currentTraceContext(
                ThreadLocalCurrentTraceContext.newBuilder()
                .addScopeDecorator(ThreadContextScopeDecorator.get())
                .build()
            )

        // Execute long-running tasks first
        GlobalScope.launch {
            // Step 1: check if spaceup was installed
            if(checkInstallation()) {
                // Step 2: create directories if not exist
                createDirectories()
                createExternalDirectories()
                // Step 3: fill sws cache
                fillSwsCache()
            }
        }

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
    }

    @NewSpan("startup-create-local-directories")
    open fun createDirectories() {
        log.info("Create local directories")
        val spaceupTempDir = spaceupLocalPathConfig.temp

        if(os.lowercase().contains(Regex("(linux|mac)"))) {
            val home = ShellLocation.HOME

            shellRun(home) {
                log.info("Create $home/$spaceupTempDir")
                command("mkdir", listOf("-p", spaceupTempDir))
            }
            // Set properties for spaceup
            System.setProperty("spaceup.tempdir", "$home/$spaceupTempDir")
        } else if (os.lowercase().contains("windows")) {
            log.info("Create ${spaceupTempDir.createNormalizedPath()}")
            Path(spaceupTempDir).normalize().createDirectories()
            System.setProperty("spaceup.tempdir", spaceupTempDir)
        }
    }

    @NewSpan("startup-create-external-directories")
    open suspend fun createExternalDirectories() {
        log.info("Create external directories")
        val dirs = listOf(spaceupRemotePathConfig.temp)

        dirs.forEach {
            log.info("create $it")

            val cmd = Command(mutableListOf("mkdir", "-p", it))
            val feedback = sshService.execute(cmd).toFeedback()
            if(!feedback.isOk()) {
                log.error(feedback.error)
            }
        }
    }

    private suspend fun checkInstallation(): Boolean {
        // Let's check if we are already installed properly
        val db = dbService.getDb()
        val serverRepo = db.getCollection<Server>()
        val server = serverRepo.find().asFlow().firstOrNull()

        var isInstalled = false
        if(server == null) {
            log.info("Seems to be first run. Set not installed!")
            val apiKey = installerService.generateAPIKey()
            val serverDocument = Server(false, apiKey)
            showApiKeyToLog(serverDocument)
            val result = serverRepo.insertOne(serverDocument).asFlow().first()
            if(!result.wasAcknowledged()) {
                log.error("Could not store Api-Key")
            }
        } else {
            val installed = server.installed
            if(!installed) {
                showApiKeyToLog(server)
            }
            isInstalled = installed
        }

        return isInstalled
    }

    @NewSpan("startup-sws-cache")
    open suspend fun fillSwsCache() {
        log.info("Update SWS cache")
        swsService.updateCache()
    }

    private fun showApiKeyToLog(server: Server) {
        colored {
            log.info("#".repeat(20).yellow)
            log.info("Finish installation with API key: ${server.apiKey.yellow.bold}")
            log.info("#".repeat(20).yellow)
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