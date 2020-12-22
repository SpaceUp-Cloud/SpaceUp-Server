package technology.iatlas.spaceup.dto

enum class ServiceOption(command: String) {
    START("start"),
    STOP("stop"),
    RESTART("restart");

    companion object {
        fun asList(): List<ServiceOption> = listOf(START, STOP, RESTART)
    }
}