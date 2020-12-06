package technology.iatlas.spaceup.controller.api

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.sse.Event
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.services.SseServiceImpl

@Controller("/time")
class ExampleSSEController {
    private val log = LoggerFactory.getLogger(ExampleSSEController::class.java)

    private var sseService: SseServiceImpl<String>
        = SseServiceImpl("update")

    @Get("/events", produces = [MediaType.TEXT_EVENT_STREAM])
    fun events(): Publisher<Event<String>> {
        return sseService.events()
    }

    @Post("/publish")
    fun publish(@Body data: String) {
        log.debug("Received data: {}", data)
        sseService.publish(data)
    }

}