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

import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.cmd.ParserInf
import technology.iatlas.spaceup.core.cmd.SshResponse
import technology.iatlas.spaceup.dto.Disk
import java.io.BufferedReader
import java.util.*

class QuotaParser: ParserInf<Disk> {
    private val log = LoggerFactory.getLogger(QuotaParser::class.java)

    override fun parseProcessOutput(processResponse: BufferedReader): Disk {
        val neededLine: Optional<String> = processResponse.lines().filter {
            it.contains("/dev")
        }.findFirst()

        return if(neededLine.isPresent) {
            parseDisk(neededLine.get())
        } else {
            Disk("", 0f, "", 0f)
        }
    }

    override fun parseSshOutput(sshResponse: SshResponse): Disk {
        val neededLine: List<String> = sshResponse.stdout.lines().filter {
            it.contains("/dev")
        }

        return if(neededLine.size == 1) {
            parseDisk(neededLine[0])
        } else {
            Disk("", 0f, "", 0f)
        }
    }

    private fun parseDisk(line: String): Disk {
        val splitted = line.replace("\\s+".toRegex(), " ")
            .split(" ")

        val spaceWithUnit = splitted[2]
        val space = spaceWithUnit.filter {
            it.isDigit()
        }

        val quotaWithUnit = splitted[3]
        val quota = quotaWithUnit.filter {
            it.isDigit()
        }

        // German decimal format
        val percentage = ((100f / Integer.valueOf(quota)) * Integer.valueOf(space))
        //val diffPercentage = DecimalFormat("#,##00.00").format((100f - percentage)).toString()
        //val usedPercentage = DecimalFormat("#,##00.00").format(percentage).toString()

        val disk = Disk(spaceWithUnit, percentage, quotaWithUnit, (100f - percentage))
        log.trace("Disk: $disk")

        return disk
    }
}