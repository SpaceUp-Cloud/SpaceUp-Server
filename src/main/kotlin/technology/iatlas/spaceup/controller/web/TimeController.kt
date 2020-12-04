package technology.iatlas.spaceup.controller.web

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.sse.Event
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.Date

@Controller("/time")
class TimeController {

    private val log = LoggerFactory.getLogger("HomeController")

    private val subject: Subject<String> = PublishSubject.create()

    @Get("/events", produces = [MediaType.TEXT_EVENT_STREAM])
    fun events(): Publisher<Event<String>> {
        //val flowable: Flowable<Long> = Flowable.interval(1, TimeUnit.SECONDS)
        //return flowable.zipWith(getDate(), this::createEvent)

        subject.ignoreElements()
        subject.onNext(LocalDateTime.now().toString())

        log.info("Executed Time Event")

        val dateFlowable = subject.hide().toFlowable(BackpressureStrategy.LATEST)
        return  dateFlowable.map(this::createEvent)
    }

    private fun createEvent(date: String): Event<String> {
        return Event.of(date)
    }

    private fun getDate(): Flowable<Date> {
        return Flowable.just(Date())
    }
}