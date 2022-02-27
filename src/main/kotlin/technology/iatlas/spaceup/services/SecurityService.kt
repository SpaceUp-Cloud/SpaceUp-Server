package technology.iatlas.spaceup.services

import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Value
import technology.iatlas.spaceup.decrypt
import technology.iatlas.spaceup.dto.db.EncryptionObject
import technology.iatlas.spaceup.encrypt

@Context
class SecurityService {
    @Value("\${micronaut.security.token.jwt.signatures.secret.generator.secret}")
    private lateinit var secret: String

    fun <T : EncryptionObject> encrypt(obj: T) {
        obj.password = obj.password.encrypt(secret)
    }

    fun <T : EncryptionObject> decrypt(obj: T, function: () -> Unit = {}) {
        obj.password = obj.password.decrypt(secret)
        function()
        obj.password = obj.password.encrypt(secret)
    }
}