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
import technology.iatlas.spaceup.dto.Server
import technology.iatlas.spaceup.dto.Ssh
import technology.iatlas.spaceup.dto.User
import java.io.File

@Context
class DbService(
    private val spaceupPathConfig: SpaceupPathConfig
) {
    private val log = LoggerFactory.getLogger(DbService::class.java)
    private lateinit var db: Nitrite
    private var dbPath: File

    init {
        val path = spaceupPathConfig.db.replaceFirst("~", System.getProperty("user.home"))
        dbPath = File(path, "spaceup.db")
    }

    fun initDb() {
        if(dbPath.isFile && dbPath.exists()) {
            openDb()
            return
        }

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
            .schemaVersion(1)
            //.addMigrations(migration1)
                //"SpaceUp", "Spac3Up!***REMOVED***"
            .openOrCreate()

        log.info("Created and migrated DB")
        log.info("Indexing fields ...")
        try {
            val userCollection = db.getRepository(User::class.java)
            if(!userCollection.hasIndex("username")) {
                log.debug("indexing 'username for user'")
                userCollection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "username")
            }
        }catch (ex : IndexingException) {
            log.warn(ex.message)
        }

        try {
            val sshCollection = db.getRepository(Ssh::class.java)
            if(!sshCollection.hasIndex("username")) {
                log.debug("indexing 'username for ssh'")
                sshCollection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "username")
            }
        }catch (ex : IndexingException) {
            log.warn(ex.message)
        }

        try {
            val serverCollection = db.getRepository(Server::class.java)
            if(!serverCollection.hasIndex("installed")) {
                log.debug("indexing 'installed for server'")
                serverCollection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "installed")
            }
        }catch (ex : IndexingException) {
            log.warn(ex.message)
        }
    }

    private fun openDb() {
        log.info("Open DB $dbPath")
        val storeModule = MVStoreModule.withConfig()
            .filePath(dbPath)
            // Differ between production and dev mode
            //.compress(true)
            .build()

        db = Nitrite
            .builder()
            .loadModule(storeModule)
            .schemaVersion(1)
                // Does not work with credentials
                // "SpaceUp", "Spac3Up!***REMOVED***"
            .openOrCreate()
    }

    fun getDb(): Nitrite {
        if(!this::db.isInitialized) {
            throw DbNotInitializedException("SpaceUp was not initialized! Run 'DbService.initDb()' first.")
        }
        return db
    }

    fun isAppInstalled(): Boolean {
        val serverRepo = db.getRepository(Server::class.java)
        val server = serverRepo.find().first()

        return server.installed
    }

    fun deleteDb() {
        val deleted = dbPath.delete()
        log.info("$dbPath was deleted: $deleted")
    }
 }

class Migration1(startVersion: Int, endVersion: Int) : Migration(startVersion, endVersion) {
    private val log = LoggerFactory.getLogger(Migration1::class.java)

    override fun migrate(instructions: Instructions?) {
        log.debug("Create 'user' repo")
        instructions
            ?.forRepository(User::class.java)
            ?.addField<String>("username")
            ?.addField<String>("password")

        log.debug("Create 'ssh' repo")
        instructions
            ?.forRepository(Ssh::class.java)
            ?.addField<String>("server")
            ?.addField<String>("username")
            ?.addField<String>("password")

        log.debug("Create 'server' repo")
        instructions
            ?.forRepository(Server::class.java)
            ?.addField("installed", false)
    }
}