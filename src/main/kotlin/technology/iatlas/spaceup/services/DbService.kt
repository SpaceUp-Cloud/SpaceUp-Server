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
import org.dizitart.kno2.nitrite
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
import technology.iatlas.spaceup.dto.db.Server
import technology.iatlas.spaceup.dto.db.Ssh
import technology.iatlas.spaceup.dto.db.User
import java.io.File

@Context
class DbService(
    spaceupPathConfig: SpaceupPathConfig
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

        val storeModule = MVStoreModule.withConfig()
            .filePath(dbPath)
                // Differ between production and dev mode
            //.compress(true)
            .build()

        //val migration1 = Migration1(0, 1)
        // Just for logging inside migration
        //migration1.steps().forEach { _ -> }

        /*db = Nitrite
            .builder()
            .loadModule(storeModule)
            .schemaVersion(1)
            //.addMigrations(migration1)
            .openOrCreate()*/

        db = nitrite("SpaceUp", "Spac3Up!#") {
            loadModule(storeModule)
        }

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
        log.info("Create or open DB $dbPath")
        val storeModule = MVStoreModule.withConfig()
            .filePath(dbPath)
            // Differ between production and dev mode
            //.compress(true)
            .build()

        /*db = Nitrite
            .builder()
            .loadModule(storeModule)
            .schemaVersion(1)
                // Does not work with credentials
                // "SpaceUp", "Spac3Up!#"
            .openOrCreate()*/

        db = nitrite ("SpaceUp", "Spac3Up!#"){
            loadModule(storeModule)
        }
    }

    fun getDb(): Nitrite {
        if(!this::db.isInitialized) {
            throw DbNotInitializedException("SpaceUp DB was not initialized! Run 'DbService.initDb()' first.")
        }
        return db
    }

    fun isAppInstalled(): Boolean {
        val serverRepo = db.getRepository(Server::class.java)
        val server = serverRepo.find().firstOrNull()

        log.debug("server object: {}", server)
        var isInstalled = false
        if(server != null) {
            isInstalled = server.installed
        }
        return isInstalled
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
            ?.addField("apiKey", "")
    }
}