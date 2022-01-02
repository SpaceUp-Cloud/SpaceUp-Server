package technology.iatlas.spaceup.dto

import org.dizitart.no2.index.IndexType
import org.dizitart.no2.repository.annotations.Entity
import org.dizitart.no2.repository.annotations.Id
import org.dizitart.no2.repository.annotations.Index

@Entity(
    value = "user",
    indices = [Index(value = ["username"], type = IndexType.UNIQUE)]
)
data class User(
    @Id
    var username: String,
    var password: String
) /*: Mappable {
    override fun write(mapper: NitriteMapper?): Document {
        val document = Document.createDocument()
        document.put("username", username)
        document.put("password", password)

        return document
    }

    override fun read(mapper: NitriteMapper?, document: Document?) {
        if(document != null) {
            username = document.get("username") as String
            password = document.get("password") as String
        }
    }
}*/