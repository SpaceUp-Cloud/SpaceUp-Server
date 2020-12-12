package technology.iatlas.spaceup.services

import technology.iatlas.spaceup.dto.FeedbackObj
import technology.iatlas.spaceup.dto.UpdatePackage
import javax.inject.Singleton


@Singleton
class UpdateServiceImpl : UpdateServiceInf {
    override fun checkFor(updatePackage: UpdatePackage): Boolean {
        TODO("Not yet implemented")
    }

    override fun executeFor(updatePackage: UpdatePackage): FeedbackObj {
        TODO("Not yet implemented")
    }
}