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

import com.mongodb.client.MongoDatabase
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Value
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.helper.colored
import technology.iatlas.spaceup.dto.db.Server

@Context
class DbService(
    spaceUpService: SpaceUpService,
    @Value("\${mongodb.uri}")
    mongoDbConnection: String
) {
    private val log = LoggerFactory.getLogger(DbService::class.java)
    private var db: MongoDatabase

    init {
        val client = KMongo.createClient(mongoDbConnection)
        log.info("Created DB Connection to $mongoDbConnection")
        db = if(spaceUpService.isDevMode()) {
            colored {
                log.info("Get development DB".yellow)
            }
            client.getDatabase("SpaceUp_Dev")
        } else {
            client.getDatabase("SpaceUp")
        }
    }

    fun getDb(): MongoDatabase {
        return db
    }

    fun init() {
        /*try {
            db.createCollection("Server")
            db.createCollection("User")
            db.createCollection("SSH")
        }catch (ex: MongoCommandException) {
            log.warn("Collections already exist!")
        }*/
    }

    fun isAppInstalled(): Boolean {
        val serverRepo = db.getCollection<Server>()
        val server = serverRepo.find().firstOrNull()

        log.debug("server object: {}", server)
        var isInstalled = false
        if(server != null) {
            isInstalled = server.installed
        }
        return isInstalled
    }

 }
