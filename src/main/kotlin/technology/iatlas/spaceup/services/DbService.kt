package technology.iatlas.spaceup.services

import io.micronaut.context.annotation.Context
import org.dizitart.no2.Nitrite
import org.dizitart.no2.exceptions.IndexingException
import org.dizitart.no2.index.IndexOptions
import org.dizitart.no2.index.IndexType
import org.dizitart.no2.migration.Instructions
import org.dizitart.no2.migration.Migration
import org.dizitart.no2.mvstore.MVStoreModule
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.config.SpaceupPathConfig
import technology.iatlas.spaceup.core.exceptions.DbNotInitializedException
import java.io.File

@Context
class DbService(
    private val spaceupPathConfig: SpaceupPathConfig
) {
    private val log = LoggerFactory.getLogger(DbService::class.java)
    private lateinit var db: Nitrite

    fun initDb() {
        val path = spaceupPathConfig.db.replaceFirst("~", System.getProperty("user.home"))
        val dbPath = File(path, "spaceup.db")
        log.info("Init DB @ $dbPath")

        val migration1 = Migration1(0, 1)

        val storeModule = MVStoreModule.withConfig()
            .filePath(dbPath)
                // Differ between production and dev mode
            //.compress(true)
            .build()

        // Just for logging inside migration
        migration1.steps().forEach { _ -> }

        db = Nitrite
            .builder()
            .loadModule(storeModule)
            .schemaVersion(0)
            .addMigrations(migration1)
            .openOrCreate("SpaceUp", "Spac3Up!#")

        log.info("Created and migrated DB")
        log.info("Indexing field ...")
        try {
            val userCollection = db.getCollection("user")
            if(!userCollection.isIndexing("username")) {
                log.debug("'username for user' already indexed")
                userCollection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "username")
            }
        }catch (ex : IndexingException) {
            log.warn(ex.message)
        }

        try {
            val sshCollection = db.getCollection("ssh")
            if(!sshCollection.isIndexing("username")) {
                log.debug("'username for ssh' already indexed")
                sshCollection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "username")
            }
        }catch (ex : IndexingException) {
            log.warn(ex.message)
        }

        try {
            val serverCollection = db.getCollection("server")
            if(!serverCollection.isIndexing("installed")) {
                log.debug("'installed for server' already indexed")
                serverCollection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "installed")
            }
        }catch (ex : IndexingException) {
            log.warn(ex.message)
        }
    }

    fun getDb(): Nitrite {
        if(!this::db.isInitialized) {
            throw DbNotInitializedException("SpaceUp was not initialized! Run 'DbService.initDb()' first.")
        }
        return db
    }
}

class Migration1(startVersion: Int, endVersion: Int) : Migration(startVersion, endVersion) {
    private val log = LoggerFactory.getLogger(Migration1::class.java)

    override fun migrate(instructions: Instructions?) {
        log.debug("Create 'user' repo")
        instructions
            ?.forRepository("user")
            ?.addField<String>("username")
            ?.addField<String>("password")

        log.debug("Create 'ssh' repo")
        instructions
            ?.forRepository("ssh")
            ?.addField<String>("server")
            ?.addField<String>("username")
            ?.addField<String>("password")

        log.debug("Create 'server' repo")
        instructions
            ?.forRepository("server")
            ?.addField("installed", false)
    }
}