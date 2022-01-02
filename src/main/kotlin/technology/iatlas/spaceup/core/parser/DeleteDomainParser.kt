/*
 * Copyright (c) 2022 Gino Atlas.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package technology.iatlas.spaceup.core.parser

import technology.iatlas.spaceup.core.cmd.ParserInf
import technology.iatlas.spaceup.core.cmd.SshResponse
import technology.iatlas.spaceup.dto.Feedback
import java.io.BufferedReader

class DeleteDomainParser(private val domain: String) : ParserInf<Feedback> {
    override fun parseProcessOutput(processResponse: BufferedReader): Feedback {
        val responseText = processResponse.readText()
        // Workaround: pack it into sshResponse
        var errorMsg = ""
        var infoMsg = ""

        if (responseText.lowercase().contains("error", true)) {
            errorMsg = responseText
        } else {
            infoMsg = responseText
        }

        return parseSshOutput(SshResponse(stdout = infoMsg, stderr = errorMsg))
    }

    override fun parseSshOutput(sshResponse: SshResponse): Feedback {
        return if(sshResponse.stderr.isNotEmpty()) {
            Feedback("", "$domain: ${sshResponse.stderr}")
        } else {
            Feedback("$domain was successfully deleted!", "")
        }
    }
}
