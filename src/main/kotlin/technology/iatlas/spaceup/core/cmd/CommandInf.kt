package technology.iatlas.spaceup.core.cmd

import technology.iatlas.spaceup.dto.SftpFile

interface CommandInf {
    val parameters: MutableList<String>
    val shellScript: SftpFile
}