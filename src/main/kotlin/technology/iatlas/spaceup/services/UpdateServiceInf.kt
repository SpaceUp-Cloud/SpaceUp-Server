package technology.iatlas.spaceup.services

import technology.iatlas.spaceup.dto.FeedbackObj
import technology.iatlas.spaceup.dto.UpdatePackage

interface UpdateServiceInf {
    /**
     * Check if an update is available for give package name
     */
    fun checkFor(updatePackage: UpdatePackage): Boolean

    /**
     * Execute update for named package
     */
    fun executeFor(updatePackage: UpdatePackage): FeedbackObj
}