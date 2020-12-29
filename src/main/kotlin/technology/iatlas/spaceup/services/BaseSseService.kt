package technology.iatlas.spaceup.services

import io.micronaut.http.sse.Event
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class BaseSseService<T> : BaseSseServiceInf<T> {
    private val log: Logger = LoggerFactory.getLogger("SseService")
    protected val subject: Subject<T> = PublishSubject.create()

    /**
     * Will create a new Event of type T
     * @param t - Generic type
     */
    override fun createEvent(t: T): Event<T> {
        return Event.of(t)
    }

    /**
     * Publish a new object to sse
     */
    override fun publish(t: T) {
        log.debug("Publish: {}", t.toString())
        subject.onNext(t)
    }

    /**
     * Get Events
     * E.g. subject.hide().toFlowable(BackpressureStrategy.LATEST).map(this::createEvent)
     */
    abstract override fun events(): Publisher<Event<T>>

}