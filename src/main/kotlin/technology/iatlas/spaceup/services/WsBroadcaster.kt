package technology.iatlas.spaceup.services

import io.micronaut.websocket.WebSocketBroadcaster
import javax.inject.Singleton

@Singleton
class WsBroadcaster(private val broadcaster: WebSocketBroadcaster) {

    /**
     * Broadcast a websocket message object with a specific topic filter
     */
    public fun <T> broadcast(message: T, filterTopic: String) {
        broadcaster.broadcast(message) {
            it.attributes.asMap()["topic"]?.equals(filterTopic) ?: false
        }
    }
}