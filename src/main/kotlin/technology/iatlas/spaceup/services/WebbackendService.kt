package technology.iatlas.spaceup.services

import io.micronaut.context.annotation.Context
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.core.parser.EchoParser
import technology.iatlas.spaceup.core.parser.ReadWebbackendParser
import technology.iatlas.spaceup.dto.Command
import technology.iatlas.spaceup.dto.Feedback
import technology.iatlas.spaceup.dto.WebbackendCmd
import technology.iatlas.spaceup.dto.WebbackendConfiguration

@Context
class WebbackendService(
    sshService: SshService,
    private val wsBroadcaster: WsBroadcaster
): WsServiceInf {
    private val log = LoggerFactory.getLogger(WebbackendService::class.java)
    override val topic = "webbackend"

    private val baseCmd = "uberspace web backend"
    private val webbackendReadRunner = Runner<List<WebbackendConfiguration>>(sshService)
    private val webbackendCreateRunner = Runner<String>(sshService)
    private val webbackendDeleteRunner = Runner<String>(sshService)

    suspend fun read(): List<WebbackendConfiguration> {
        val webbackendConfigurations = mutableListOf<WebbackendConfiguration>()
        webbackendReadRunner.subject().subscribe {
            wsBroadcaster.broadcast(it, topic)
            webbackendConfigurations.addAll(it)
        }

        val readCmd: MutableList<String> = mutableListOf(baseCmd, "list")
        webbackendReadRunner.execute(Command(readCmd), ReadWebbackendParser())

        return webbackendConfigurations
    }

    suspend fun create(webbackendCmd: WebbackendCmd): Feedback {
        val feedback = Feedback("", "")
        webbackendCreateRunner.subject().subscribeBy(
            onNext = {
                if(it.lowercase().contains("error")) {
                    feedback.error = it
                } else {
                    feedback.info = it
                }
                wsBroadcaster.broadcast(feedback, topic)
            },
            onError = {
                feedback.error = "Could not create web backend configuration: "
                val errMsg = it.message
                if(errMsg != null) {
                    feedback.error += errMsg
                }
            }
        )
        val createCmd: MutableList<String> = mutableListOf(baseCmd, "set")
        if(webbackendCmd.isApache) {
            createCmd.add("--apache")
        } else if(webbackendCmd.isHttp) {
            createCmd.add("--http")
        } else if(webbackendCmd.removePrefix) {
            createCmd.add("--remove-prefix")
        } else if(webbackendCmd.port != null) {
            createCmd.add("--port ${webbackendCmd.port}")
        }
        createCmd.add(webbackendCmd.url)

        webbackendCreateRunner.execute(Command(createCmd), EchoParser())
        return feedback
    }

    suspend fun delete(domain: String): Feedback {
        val feedback = Feedback("", "")
        webbackendDeleteRunner.subject().subscribe {
            // response: This backend does not exist. For more information ...
            if(it.lowercase().contains("does not exist")) {
                feedback.error = it
            } else {
                feedback.info = "Successfully deleted web backend for $domain"
            }
            wsBroadcaster.broadcast(feedback, topic)
        }
        val createCmd = mutableListOf(baseCmd, "del", domain)
        webbackendDeleteRunner.execute(Command(createCmd), EchoParser())

        return feedback
    }
}