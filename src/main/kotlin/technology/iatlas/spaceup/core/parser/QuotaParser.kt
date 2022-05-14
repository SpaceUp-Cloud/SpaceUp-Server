/*
 * Copyright (c) 2022 spaceup@iatlas.technology.
 * SpaceUp-Server is free software; You can redistribute it and/or modify it under the terms of:
 *   - the GNU Affero General Public License version 3 as published by the Free Software Foundation.
 * You don't have to do anything special to accept the license and you don’t have to notify anyone which that you have made that decision.
 *
 * SpaceUp-Server is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See your chosen license for more details.
 *
 * You should have received a copy of both licenses along with SpaceUp-Server
 * If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * There is a strong belief within us that the license we have chosen provides not only the best solution for providing you with the essential freedom necessary to use SpaceUp-Server within your projects, but also for maintaining enough copyleft strength for us to feel confident and secure with releasing our hard work to the public. For your convenience we've included our own interpretation of the license we chose, which can be seen below.
 *
 * Our interpretation of the GNU Affero General Public License version 3: (Quoted words are words in which there exists a definition within the license to avoid ambiguity.)
 *   1. You must always provide the source code, copyright and license information of SpaceUp-Server whenever you "convey" any part of SpaceUp-Server;
 *      be it a verbatim copy or a modified copy.
 *   2. SpaceUp-Server was developed as a library and has therefore been designed without knowledge of your work; as such the following should be implied:
 *      a) SpaceUp-Server was developed without knowledge of your work; as such the following should be implied:
 *         i)  SpaceUp-Server should not fall under a work which is "based on" your work.
 *         ii) You should be free to use SpaceUp-Server in a work covered by the:
 *             - GNU General Public License version 2
 *             - GNU Lesser General Public License version 2.1
 *             This is due to those licenses classifying SpaceUp-Server as a work which would fall under an "aggregate" work by their terms and definitions;
 *             as such it should not be covered by their terms and conditions. The relevant passages start at:
 *             - Line 129 of the GNU General Public License version 2
 *             - Line 206 of the GNU Lesser General Public License version 2.1
 *      b) If you have not "modified", "adapted" or "extended" SpaceUp-Server then your work should not be bound by this license,
 *         as you are using SpaceUp-Server under the definition of an "aggregate" work.
 *      c) If you have "modified", "adapted" or "extended" SpaceUp-Server then any of those modifications/extensions/adaptations which you have made
 *         should indeed be bound by this license, as you are using SpaceUp-Server under the definition of a "based on" work.
 *
 * Our hopes is that our own interpretation of license aligns perfectly with your own values and goals for using our work freely and securely. If you have any questions at all about the licensing chosen for SpaceUp-Server you can email us directly at spaceup@iatlas.technology or you can get in touch with the license authors (the Free Software Foundation) at licensing@fsf.org to gain their opinion too.
 *
 * Alternatively you can provide feedback and acquire the support you need at our support forum. We'll definitely try and help you as soon as possible, and to the best of our ability; as we understand that user experience is everything, so we want to make you as happy as possible! So feel free to get in touch via our support forum and chat with other users of SpaceUp-Server here at:
 * https://spaceup.iatlas.technology
 *
 * Thanks, and we hope you enjoy using SpaceUp-Server and that it's everything you ever hoped it could be.
 */

package technology.iatlas.spaceup.core.parser

import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.cmd.ParserInf
import technology.iatlas.spaceup.core.cmd.SshResponse
import technology.iatlas.spaceup.dto.Disk
import java.io.BufferedReader
import java.util.*
import kotlin.math.roundToInt

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

        val percentageTwoDecimals = (percentage * 100.0).roundToInt() / 100.0
        val disk = Disk(spaceWithUnit, percentageTwoDecimals.toFloat(), quotaWithUnit, (100 - percentageTwoDecimals).toFloat())
        log.trace("Disk: $disk")

        return disk
    }
}