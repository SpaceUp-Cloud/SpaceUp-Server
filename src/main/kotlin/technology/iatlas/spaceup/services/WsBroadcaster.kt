package technology.iatlas.spaceup.services

import io.micronaut.websocket.WebSocketBroadcaster
import jakarta.inject.Singleton

@Singleton
class WsBroadcaster(private val broadcaster: WebSocketBroadcaster) {

    /**
     * Broadcast synced a websocket message object with a specific topic filter
     */
    public fun <T> broadcastSync(message: T, filterTopic: String) {
        broadcaster.broadcastSync(message) {
            it.attributes.asMap()["topic"]?.equals(filterTopic) ?: false
        }
    }

    /**
     * Broadcast synced a websocket message object with a specific topic filter
     * @see WsBroadcaster.broadcastSync
     */
    public fun <T> broadcast(message: T, filterTopic: String) {
        broadcaster.broadcastSync(message) {
            it.attributes.asMap()["topic"]?.equals(filterTopic) ?: false
        }
    }

    /**
     * Broadcast async a websocket message object with a specific topic filter
     */
    public fun <T> broadcastAsync(message: T, filterTopic: String) {
        broadcaster.broadcastAsync(message) {
            it.attributes.asMap()["topic"]?.equals(filterTopic) ?: false
        }
    }
}