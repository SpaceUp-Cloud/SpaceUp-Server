/*
 * Copyright (c) 2022 Gino Atlas.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package technology.iatlas.spaceup.dto

import org.dizitart.no2.index.IndexType
import org.dizitart.no2.repository.annotations.Entity
import org.dizitart.no2.repository.annotations.Id
import org.dizitart.no2.repository.annotations.Index

@Entity(
    value = "ssh",
    indices = [Index(value = ["username"], type = IndexType.UNIQUE)]
)
data class Ssh(
    @Id
    var username: String,
    var password: String,
    var server: String
) /*: Mappable {
    override fun write(mapper: NitriteMapper?): Document {
        val document = Document.createDocument()
        document.put("username", username)
        document.put("password", password)
        document.put("server", server)

        return document
    }

    override fun read(mapper: NitriteMapper?, document: Document?) {
        if(document != null) {
            username = document.get("username") as String
            password = document.get("password") as String
            server = document.get("server") as String
        }
    }
}*/