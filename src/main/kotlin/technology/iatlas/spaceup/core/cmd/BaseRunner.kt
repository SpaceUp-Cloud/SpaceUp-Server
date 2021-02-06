package technology.iatlas.spaceup.core.cmd

import io.reactivex.rxjava3.subjects.PublishSubject

abstract class BaseRunner<T>: RunnerInf<T> {
    protected val subject: PublishSubject<T> = PublishSubject.create()

    final override fun subject(): PublishSubject<T> {
        return subject
    }
}