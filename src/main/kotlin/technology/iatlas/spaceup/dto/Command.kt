package technology.iatlas.spaceup.dto

import technology.iatlas.spaceup.core.cmd.CommandInf

data class Command(override val parameters: MutableList<String>,
                   override val shellScript: SftpFile = SftpFile("", null, false)) : CommandInf