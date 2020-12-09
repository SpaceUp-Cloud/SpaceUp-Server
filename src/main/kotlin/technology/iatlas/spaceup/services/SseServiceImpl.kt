package technology.iatlas.spaceup.services

import io.micronaut.http.sse.Event
import io.reactivex.BackpressureStrategy
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class SseServiceImpl<T>(private val eventName: String): BaseSseService<T>() {
    private val log = LoggerFactory.getLogger(SseServiceImpl::class.java)

    override fun events(): Publisher<Event<T>> {
        val res = subject.hide()
            .toFlowable(BackpressureStrategy.LATEST).map(this::createEvent)

        res.forEach {
            log.debug(it.data.toString())
        }

        return res
    }

    /**
     * Create a specific event element
     */
    override fun createEvent(t: T): Event<T> {
        return Event.of(t).name(eventName)
    }

}