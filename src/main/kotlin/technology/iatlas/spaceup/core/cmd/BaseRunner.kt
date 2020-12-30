package technology.iatlas.spaceup.core.cmd

import io.reactivex.rxjava3.subjects.BehaviorSubject

abstract class BaseRunner<T>: RunnerInf<T> {

    protected val subject: BehaviorSubject<T> = BehaviorSubject.create()

    override fun getBehaviourSubject(): BehaviorSubject<T> {
        return subject
    }
}