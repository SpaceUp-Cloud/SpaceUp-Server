/*
 * Copyright (c) 2022 Gino Atlas.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package technology.iatlas.spaceup.services

import io.micronaut.context.annotation.Context
import io.micronaut.websocket.WebSocketBroadcaster

@Context
class WsBroadcaster(private val broadcaster: WebSocketBroadcaster) {

    /**
     * Broadcast synced a websocket message object with a specific topic filter
     */
    fun <T> broadcastSync(message: T, filterTopic: String) {
        broadcaster.broadcastSync(message) {
            it.attributes.asMap()["topic"]?.equals(filterTopic) ?: false
        }
    }

    /**
     * Broadcast synced a websocket message object with a specific topic filter
     * @see WsBroadcaster.broadcastSync
     */
    fun <T> broadcast(message: T, filterTopic: String) {
        broadcaster.broadcastSync(message) {
            it.attributes.asMap()["topic"]?.equals(filterTopic) ?: false
        }
    }

    /**
     * Broadcast async a websocket message object with a specific topic filter
     */
    fun <T> broadcastAsync(message: T, filterTopic: String) {
        broadcaster.broadcastAsync(message) {
            it.attributes.asMap()["topic"]?.equals(filterTopic) ?: false
        }
    }
}