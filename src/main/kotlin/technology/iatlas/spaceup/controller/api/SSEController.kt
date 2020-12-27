package technology.iatlas.spaceup.controller.api

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.sse.Event
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.services.SseService

@Controller("/api/sse")
class SSEController(private val sseService: SseService<String>) {
    private val log = LoggerFactory.getLogger(SSEController::class.java)

    init {
        sseService.eventName = "update"
    }

    @Get("/events", produces = [MediaType.TEXT_EVENT_STREAM])
    fun events(): Publisher<Event<String>> {
        return sseService.events()
    }
}