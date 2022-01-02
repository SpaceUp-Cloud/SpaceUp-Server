package technology.iatlas.spaceup.dto

import org.dizitart.no2.collection.Document
import org.dizitart.no2.common.mapper.Mappable
import org.dizitart.no2.common.mapper.NitriteMapper
import org.dizitart.no2.index.IndexType
import org.dizitart.no2.repository.annotations.Entity
import org.dizitart.no2.repository.annotations.Id
import org.dizitart.no2.repository.annotations.Index

@Entity(
    value = "server",
    indices = [Index(value = ["installed"], type = IndexType.UNIQUE)]
)
data class Server(
    @Id
    var installed: Boolean = false,
    var apiKey: String
) : Mappable {
    override fun write(mapper: NitriteMapper?): Document {
        val document = Document.createDocument()
        document.put("installed", installed)
        if(apiKey.isNotEmpty()) {
            document.put("apiKey", apiKey)
        }

        return document
    }

    override fun read(mapper: NitriteMapper?, document: Document?) {
        if(document != null) {
            installed = document.get("installed") as Boolean
            val apiKey = document.get("apiKey")
            if(apiKey == null) {
                this.apiKey = ""
            } else {
                this.apiKey = apiKey as String
            }
        }
    }
}