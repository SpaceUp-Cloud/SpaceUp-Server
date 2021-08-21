package technology.iatlas.spaceup.services

import jakarta.inject.Singleton
import technology.iatlas.spaceup.dto.Feedback
import technology.iatlas.spaceup.dto.UpdatePackage


@Singleton
class UpdateService : UpdateServiceInf {
    override fun checkFor(updatePackage: UpdatePackage): Boolean {
        TODO("Not yet implemented")
    }

    override fun executeFor(updatePackage: UpdatePackage): Feedback {
        TODO("Not yet implemented")
    }
}