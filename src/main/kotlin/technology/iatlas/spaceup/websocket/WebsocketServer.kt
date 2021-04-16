package technology.iatlas.spaceup.websocket

import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.http.annotation.PathVariable
import io.micronaut.websocket.CloseReason
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.*
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.dto.Feedback
import technology.iatlas.spaceup.events.WebsocketFeedbackResponseEvent

@ServerWebSocket("/ws/{topic}")
class WebsocketServer: ApplicationEventListener<WebsocketFeedbackResponseEvent>{
    private val log = LoggerFactory.getLogger(WebsocketServer::class.java)
    private val sessions = hashMapOf<String, WebSocketSession>()

    @OnOpen
    fun onOpen(session: WebSocketSession, @PathVariable topic: String) {
        log.info("Websocket open for ${session.id} on topic $topic")
        session.attributes.put("topic", topic)
        sessions[session.id] = session
    }

    @OnMessage
    fun onMessage(message: String, session: WebSocketSession, @PathVariable topic: String) {
        log.debug("Websocket received message $message for topic $topic")
    }

    @OnClose
    fun onClose(closeReason: CloseReason, session: WebSocketSession, @PathVariable topic: String) {
        log.warn("Websocket for $topic closed because: $closeReason")
        session.remove(session.id)
    }

    @OnError
    fun onError(t: Throwable, @PathVariable topic: String) {
        log.error("Websocket on $topic an error happened because ${t.message}")
    }

    /**
     * Will be used to send messages to the client
     */
    private fun publish(message: Feedback?, session: WebSocketSession) {
        if(session.isOpen) {
            session.sendAsync(message)
        }
    }

    // FIXME: We can use the broadcaster to send messages
    override fun onApplicationEvent(eventFeedback: WebsocketFeedbackResponseEvent?) {
        log.debug("Broadcast message: $eventFeedback")

        var feedback = Feedback("", "")
        if(eventFeedback?.source is Feedback) {
            feedback = eventFeedback.source as Feedback
            log.debug("Send Feedback: $feedback")
        }

        sessions.forEach { (_, session) ->
            sessions[session.id]?.let { publish(feedback, it) }
        }
    }
}