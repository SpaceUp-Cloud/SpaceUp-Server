package technology.iatlas.spaceup.dto

data class WebbackendConfiguration(
    val web: String,
    val process: String? = "",
    val service: String? = ""
)
