package technology.iatlas.spaceup.websocket

import io.micronaut.websocket.WebSocketBroadcaster
import io.micronaut.websocket.annotation.*
import org.slf4j.LoggerFactory

@ServerWebSocket("/ws/{topic}")
class WebsocketHandler(private val broadcaster: WebSocketBroadcaster) {

    private val log = LoggerFactory.getLogger(WebsocketHandler::class.java)

    @OnOpen
    fun onOpen() {
    }

    @OnMessage
    fun onMessage() {
    }

    @OnClose
    fun onClose() {
    }

    @OnError
    fun onError(t: Throwable) {
        log.error("Something bad happened on WS: {}", t.message)
    }

}