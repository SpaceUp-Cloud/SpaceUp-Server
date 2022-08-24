/*
 * Copyright (c) 2022 Gino Atlas.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package technology.iatlas.spaceup.services

import io.micronaut.context.annotation.Context
import io.micronaut.context.env.Environment

@Context
class SpaceUpService(
    private val env: Environment
    // TODO: get Micronaut properties. Dev mode should only possible when it is running on localhost
) {

    fun isDevMode(): Boolean {
        val currentEnv = env.activeNames
        var isDev = false
        currentEnv.forEach {
            val environment = it.lowercase()
            if(environment.contains("dev")
                || environment.contains("develop")
                || environment.contains("development")) {
                isDev = true
            }
        }

        return isDev
    }

    fun getSpaceUpVersion(): String {
        val versionProperty = Properties()
        versionProperty.load(this.javaClass.getResourceAsStream("/version.properties"))
        return versionProperty.getProperty("version") ?: "no version"
    }
}