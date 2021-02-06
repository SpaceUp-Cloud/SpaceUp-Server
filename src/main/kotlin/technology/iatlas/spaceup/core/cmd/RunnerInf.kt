package technology.iatlas.spaceup.core.cmd

import io.reactivex.rxjava3.subjects.PublishSubject

interface RunnerInf<T> {
    suspend fun execute(cmd: CommandInf, parser: ParserInf<T>)
    fun subject(): PublishSubject<T>
}