package technology.iatlas.spaceup.services

/**
 * Simple interface which determines broadcasting of websocket messages
 */
interface WsServiceInf {
    // The topic is used as filter for the websocket endpoint!
    val topic: String
}