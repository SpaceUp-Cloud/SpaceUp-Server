package technology.iatlas.spaceup.services

import io.reactivex.rxjava3.subjects.PublishSubject
import javax.inject.Singleton

@Singleton
class WSService<T> {
    private val subject = PublishSubject.create<T>()

    fun send(text: T) {
        subject.onNext(text)
    }

    fun getSubject(): PublishSubject<T> {
        return subject
    }
}