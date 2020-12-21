package technology.iatlas.spaceup.dto

data class Navigation(
    val linkname: String,
    val mapping: String,
    val prio: Int,
    val newTab: Boolean = false
)
