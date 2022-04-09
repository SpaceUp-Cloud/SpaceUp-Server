package technology.iatlas.spaceup.dto

data class WebbackendCmd(
    val url: String,
    val isApache: Boolean = false,
    val isHttp: Boolean = false,
    val removePrefix: Boolean = false,
    val port: Int?
)
