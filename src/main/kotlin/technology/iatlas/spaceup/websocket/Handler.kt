package technology.iatlas.spaceup.websocket

interface Handler {
    fun receive(input: String)
    fun send(output: String)
}