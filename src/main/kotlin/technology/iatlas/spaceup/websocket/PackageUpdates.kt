package technology.iatlas.spaceup.websocket

import io.micronaut.websocket.WebSocketBroadcaster

class PackageUpdates(private val broadcaster: WebSocketBroadcaster) : Handler {
    override fun receive(input: String) {
        TODO("Not yet implemented")
    }

    override fun send(output: String) {
        TODO("Not yet implemented")
    }
}