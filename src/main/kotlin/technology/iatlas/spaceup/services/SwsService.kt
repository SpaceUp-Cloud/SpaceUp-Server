package technology.iatlas.spaceup.services

import com.mongodb.client.MongoCollection
import io.micronaut.context.annotation.Context
import org.litote.kmongo.eq
import org.litote.kmongo.getCollection
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.dto.Feedback
import technology.iatlas.spaceup.dto.db.Sws
import technology.iatlas.spaceup.isOk
import technology.iatlas.sws.SWSCreator
import technology.iatlas.sws.objects.ParserException
import java.io.File

@Context
class SwsService(
    private val dbService: DbService,
) {
    private val log = LoggerFactory.getLogger(SwsService::class.java)

    private fun validateSWS(sws: Sws, feedback: Feedback) {
        log.info("Validate sws")
        // Create a temporary file
        val home = System.getProperty("user.home")
        val swsFilePath = "$home/.spaceup/tmp/${sws.name}.sws"

        val swsFile = File(swsFilePath)
        swsFile.writeText(sws.content)

        try {
            // If it throws an exception, the content is corrupt
            SWSCreator.createAndParse(swsFile)
        }catch (ex: ParserException) {
            log.error(ex.message)
            feedback.error = "SWS content is corrupt! ${ex.message}"
        }

        swsFile.delete()
    }

    private fun findCheckAndExecute(
        sws: Sws,
        swsRepo: MongoCollection<Sws>,
        executionName: String,
        executeCallback: (feedback: Feedback, exists: Boolean) -> Unit): Feedback {
        val feedback = Feedback("", "")

        // Check if sws is valid
        validateSWS(sws, feedback)
        if(!feedback.isOk()) {
            return feedback
        }

        val found = swsRepo.find(Sws::name eq sws.name).first() != null
        log.debug("${sws.name} found: $found")
        log.info("Will execute '$executionName' with server web script '${sws.name}'.")
        executeCallback(feedback, found)

        return feedback
    }

    fun create(sws: Sws): Feedback {
        log.info("Check if sws ${sws.name} already exists.")
        val db = dbService.getDb()
        val swsRepo = db.getCollection<Sws>()

        return findCheckAndExecute(sws, swsRepo, "create") { feedback, found ->
            val errorCase = "${sws.name} already exists!"

            // Should not exit && validation was fine
            if (!found && feedback.isOk()) {
                val result = swsRepo.insertOne(sws)
                if (result.wasAcknowledged()) {
                    feedback.info = "Created ${sws.name} successfully"
                } else {
                    feedback.error = errorCase
                }
            } else {
                feedback.error = errorCase
            }
        }
    }

    fun getAll(): List<Sws> {
        log.info("Get all SWS configurations")

        val db = dbService.getDb()
        val swsRepo = db.getCollection<Sws>()

        // Find all SWS configurations
        val allSws = swsRepo.find()
        return allSws.toList()
    }

    fun update(sws: Sws): Feedback {
        log.info("Update $sws on database")
        val db = dbService.getDb()
        val swsRepo = db.getCollection<Sws>()

        return findCheckAndExecute(sws, swsRepo, "update") { feedback, found ->
            val errorCase = "Could not update ${sws.name} as it already exist!"

            if(found && feedback.isOk()) {
                val result = swsRepo.replaceOne(Sws::name eq sws.name, sws)
                if(result.wasAcknowledged() && result.matchedCount > 0) {
                    feedback.info = "updated ${sws.name} successfully"
                } else {
                    feedback.error = errorCase
                }
            } else {
                feedback.error = errorCase
            }
        }
    }

    fun delete(name: String): Feedback {
        log.info("Delete $name on database")
        val feedback = Feedback("", "")

        val db = dbService.getDb()
        val swsRepo = db.getCollection<Sws>()

        val errorCase = "Could not delete sws $name"

        val result = swsRepo.deleteOne(Sws::name eq name)
        if(result.deletedCount == 1L && result.wasAcknowledged()) {
            feedback.info = "Deleted $name successfully"
        } else {
            feedback.error = errorCase
        }

        return feedback
    }
}