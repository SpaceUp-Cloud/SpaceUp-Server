package technology.iatlas.spaceup.dto.db

import io.micronaut.core.annotation.ReflectiveAccess

@ReflectiveAccess
data class Sws(
    var name: String,
    var content: String
)
