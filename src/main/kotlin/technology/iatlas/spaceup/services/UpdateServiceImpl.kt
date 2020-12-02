package technology.iatlas.spaceup.services

import technology.iatlas.spaceup.dto.FeedbackObj
import technology.iatlas.spaceup.dto.UpdatePackage
import javax.inject.Singleton


@Singleton
class UpdateServiceImpl : UpdateService {
    override fun checkFor(pck: UpdatePackage): Boolean {
        TODO("Not yet implemented")
    }

    override fun executeFor(pck: UpdatePackage): FeedbackObj {
        TODO("Not yet implemented")
    }
}