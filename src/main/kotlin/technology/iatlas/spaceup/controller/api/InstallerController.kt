/*
 * Copyright (c) 2022 Gino Atlas.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package technology.iatlas.spaceup.controller.api

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import technology.iatlas.spaceup.core.annotations.Installation
import technology.iatlas.spaceup.dto.db.Ssh
import technology.iatlas.spaceup.dto.db.User
import technology.iatlas.spaceup.services.InstallerService

/**
 * This is the installer endpoint to create a proper application setup.
 * The endpoint will only be reachable when there isn't an installation.
 */
@Installation
@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/api/installer")
class InstallerController(private val installerService: InstallerService) {
    /**
     * Create a new user for authentication on SpaceUp
     * @param User defines a user database object
     */
    @Post(uri = "/createUser", processes = [MediaType.APPLICATION_JSON])
    fun createUser(@Body user: User, @Header("X-SpaceUp-Key") apiKey: String): HttpResponse<String> {
        if(installerService.getApiKey() != apiKey) {
            return HttpResponse.status(HttpStatus.EXPECTATION_FAILED, "API Key is not valid!")
        }

        return installerService.createUser(user)
    }

    /**
     * Create the ssh user for authentication via SSH
     * @param SshUser defines ssh user database object
     */
    @Post(uri = "/createSshSetup", processes = [MediaType.APPLICATION_JSON])
    fun createSshUser(@Body ssh: Ssh, @Header("X-SpaceUp-Key") apiKey: String): HttpResponse<String> {
        if(installerService.getApiKey() != apiKey) {
            return HttpResponse.status(HttpStatus.EXPECTATION_FAILED, "API Key is not valid!")
        }

        return installerService.createSshUser(ssh)
    }

    /**
     * This is the final step to finalize the installation. It needs to be run.
     */
    @Post(uri = "/final")
    suspend fun finalize(@Header("X-SpaceUp-Key") apiKey: String): HttpResponse<String>{
        if(installerService.getApiKey() != apiKey) {
            return HttpResponse.status(HttpStatus.EXPECTATION_FAILED, "API Key is invalid")
        }

        return installerService.finalizeInstallation()
    }
}