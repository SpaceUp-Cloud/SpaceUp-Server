package technology.iatlas.spaceup.core.parser

import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.cmd.ParserInf
import technology.iatlas.spaceup.dto.Disk
import java.io.BufferedReader
import java.util.*

class QuotaParser: ParserInf<Disk?> {
    private val log = LoggerFactory.getLogger(QuotaParser::class.java)

    override fun parse(cmdOutput: BufferedReader): Disk {
        val neededLine: Optional<String> = cmdOutput.lines().filter {
            it.contains("/dev")
        }.findFirst()

        val diskQuota: Disk
        if(neededLine.isPresent) {
            val splitted: List<String> = neededLine.get()
                .replace("\\s+".toRegex(), " ")
                .split(" ")

            val spaceWithUnit = splitted[2]
            val space = spaceWithUnit.filter {
                it.isDigit()
            }

            val quotaWithUnit = splitted[3]
            val quota = quotaWithUnit.filter {
                it.isDigit()
            }

            log.debug("Space as string: $spaceWithUnit")
            log.debug("Quota as string: $quotaWithUnit")

            val inPercent = ((100f / Integer.valueOf(quota)) * Integer.valueOf(space))

            diskQuota = Disk(spaceWithUnit, quotaWithUnit, inPercent)
        } else {
            diskQuota = Disk("", "",0f)
        }

        return diskQuota
    }

    override fun parseText(cmdOutput: String): Disk {
        val neededLine: List<String> = cmdOutput.lines().filter {
            it.contains("/dev")
        }

        val diskQuota: Disk
        if(neededLine.size == 1) {
            val splitted: List<String> = neededLine.get(0)
                .replace("\\s+".toRegex(), " ")
                .split(" ")

            val spaceWithUnit = splitted[2]
            val space = spaceWithUnit.filter {
                it.isDigit()
            }

            val quotaWithUnit = splitted[3]
            val quota = quotaWithUnit.filter {
                it.isDigit()
            }

            log.debug("Space as string: $spaceWithUnit")
            log.debug("Quota as string: $quotaWithUnit")

            val inPercent = ((100f / Integer.valueOf(quota)) * Integer.valueOf(space))

            diskQuota = Disk(spaceWithUnit, quotaWithUnit, inPercent)
        } else {
            diskQuota = Disk("", "",0f)
        }

        return diskQuota
    }
}