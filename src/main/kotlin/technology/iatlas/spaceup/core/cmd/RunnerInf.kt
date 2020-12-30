package technology.iatlas.spaceup.core.cmd

import io.reactivex.rxjava3.subjects.BehaviorSubject

interface RunnerInf<T> {
    suspend fun execute(cmd: CommandInf, parser: ParserInf<T>)
    fun getBehaviourSubject(): BehaviorSubject<T>
}