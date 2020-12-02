package technology.iatlas.spaceup.services

import technology.iatlas.spaceup.dto.FeedbackObj
import technology.iatlas.spaceup.dto.UpdatePackage

interface UpdateService {
    /**
     * Check if an update is available for give package name
     */
    fun checkFor(pck: UpdatePackage): Boolean

    /**
     * Execute update for named package
     */
    fun executeFor(pck: UpdatePackage): FeedbackObj
}