package technology.iatlas.spaceup.services

import io.micronaut.context.annotation.Context
import org.dizitart.no2.Nitrite
import org.dizitart.no2.NitriteBuilder
import org.dizitart.no2.mvstore.MVStoreModule

@Context
class DbService {
    private var db: NitriteBuilder

    init {
        val tempdir = System.getProperty("java.io.tmpdir")

        val storeModule = MVStoreModule.withConfig()
            .filePath("$tempdir + /.spaceup.db")
            .compress(true)
            .build()

        db = Nitrite
            .builder()
            .loadModule(storeModule)

    }

    fun createOrOpen(): NitriteBuilder {
        return db
    }
}