package technology.iatlas.spaceup.core.cmd

interface RunnerInf<T> {
    fun execute(cmd: CommandInf, parser: ParserInf<T>): T?
}