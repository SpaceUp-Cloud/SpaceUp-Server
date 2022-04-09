/*
 * Copyright (c) 2022 Gino Atlas.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package technology.iatlas.spaceup.websocket

import io.micronaut.http.annotation.PathVariable
import io.micronaut.websocket.CloseReason
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.*
import org.slf4j.LoggerFactory

@ServerWebSocket("/ws/{topic}")
class WebsocketServer{
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
    /*private fun publish(message: Feedback?, session: WebSocketSession) {
        if(session.isOpen) {
            session.sendAsync(message)
        }
    }*/
}