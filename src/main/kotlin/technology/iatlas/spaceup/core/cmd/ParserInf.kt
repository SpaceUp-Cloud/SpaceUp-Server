package technology.iatlas.spaceup.core.cmd

interface ParserInf<out T> {
    fun parse(input: String): T
}
