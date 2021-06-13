package technology.iatlas.spaceup.dto

import java.net.URL

data class SftpFile(
    val name: String,
    val scriptPath: URL?,
    val doExecute: Boolean = false
)
