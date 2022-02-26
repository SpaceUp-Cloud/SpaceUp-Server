/*
 * Copyright (c) 2022 Gino Atlas.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package technology.iatlas.spaceup.core.interceptor

import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import jakarta.inject.Singleton
import org.litote.kmongo.getCollection
import technology.iatlas.spaceup.core.annotations.Installation
import technology.iatlas.spaceup.dto.db.Server
import technology.iatlas.spaceup.services.DbService

@Singleton
@InterceptorBean(Installation::class)
class InstallationInterceptor(
    private val dbService: DbService
) : MethodInterceptor<Any, Any> {

    override fun intercept(context: MethodInvocationContext<Any, Any>?): Any? {
        val db = dbService.getDb()
        val serverRepo = db.getCollection<Server>()

        val server = serverRepo.find().first()!!
        if(server.installed) {
            /*throw InstalledException("App is already installed. " +
                    "Remove spaceup.db and restart Server if you need to.")*/

            return HttpResponse.status<String>(HttpStatus.EXPECTATION_FAILED, "App is already installed!")
        }

        return context?.proceed()
    }
}