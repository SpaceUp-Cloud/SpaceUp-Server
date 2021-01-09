package technology.iatlas.spaceup.core.parser

import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.cmd.ParserInf
import technology.iatlas.spaceup.dto.Disk
import java.io.BufferedReader
import java.text.DecimalFormat
import java.util.*

class QuotaParser: ParserInf<Disk?> {
    private val log = LoggerFactory.getLogger(QuotaParser::class.java)

    override fun parse(cmdOutput: BufferedReader): Disk {
        val neededLine: Optional<String> = cmdOutput.lines().filter {
            it.contains("/dev")
        }.findFirst()

        return if(neededLine.isPresent) {
            parseDisk(neededLine.get())
        } else {
            Disk("", 0f, "", 0f)
        }
    }

    override fun parseText(cmdOutput: String): Disk {
        val neededLine: List<String> = cmdOutput.lines().filter {
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
        //val diffPercentage = DecimalFormat("***REMOVED***,***REMOVED******REMOVED***00.00").format((100f - percentage)).toString()
        //val usedPercentage = DecimalFormat("***REMOVED***,***REMOVED******REMOVED***00.00").format(percentage).toString()

        val disk = Disk(spaceWithUnit, percentage, quotaWithUnit, (100f - percentage))
        log.trace("Disk: $disk")

        return disk
    }
}