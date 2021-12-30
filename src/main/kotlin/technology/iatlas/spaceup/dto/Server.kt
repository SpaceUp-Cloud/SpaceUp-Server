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
    var installed: Boolean = false
) : Mappable {
    override fun write(mapper: NitriteMapper?): Document {
        val document = Document.createDocument()
        document.put("installed", installed)

        return document
    }

    override fun read(mapper: NitriteMapper?, document: Document?) {
        if(document != null) {
            installed = document.get("installed") as Boolean
        }
    }

}