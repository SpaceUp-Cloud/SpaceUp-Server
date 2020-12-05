package technology.iatlas.spaceup.services

import io.micronaut.http.sse.Event
import org.reactivestreams.Publisher

interface BaseSseServiceInf<T> {

    fun publish(t: T)
    fun events(): Publisher<Event<T>>
    fun createEvent(t: T): Event<T>

}