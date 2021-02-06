package technology.iatlas.spaceup.events

import io.micronaut.context.event.ApplicationEvent
import technology.iatlas.spaceup.dto.Feedback

class WebsocketFeedbackResponseEvent(feedback: Feedback) : ApplicationEvent(feedback) {

}