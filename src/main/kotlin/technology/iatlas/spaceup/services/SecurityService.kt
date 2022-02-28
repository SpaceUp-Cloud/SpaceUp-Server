package technology.iatlas.spaceup.services

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import technology.iatlas.spaceup.core.annotations.Encryption
import technology.iatlas.spaceup.decrypt
import technology.iatlas.spaceup.encrypt

/**
 * En-/decrypt POJOs with ease
 */
@Singleton
class SecurityService {
    //private val logger = LoggerFactory.getLogger(SecurityService::class.java)

    @Value("\${micronaut.security.token.jwt.signatures.secret.generator.secret}")
    private lateinit var secret: String

    /**
     * Encrypts any the field annotated with '@field:Encryption'
     * @param obj requires a POJO with annotated Encryption field
     * @see Encryption
     */
    fun <T> encrypt(obj: T) {
        val fields = obj!!::class.java.declaredFields
        fields.forEach { field ->
            if(field.isAnnotationPresent(Encryption::class.java)) {
                field.isAccessible = true
                val value = field.get(obj) as String
                field.set(obj, value.encrypt(secret))
            }
        }
    }

    /**
     * Decrypts any the field annotated with '@field:Encryption'
     * @param obj requires a POJO with annotated Encryption field
     * @param function Callback function to execute between decryption and encryption
     * @see Encryption
     */
    fun <T> decrypt(obj: T, function: () -> Unit = {}) {
        val fields = obj!!::class.java.declaredFields
        fields.forEach { field ->
            if(field.isAnnotationPresent(Encryption::class.java)) {
                field.isAccessible = true
                val value = field.get(obj) as String
                field.set(obj, value.decrypt(secret))
            }
        }
        function()
        //Re-encrypt after usage
        encrypt(obj)
    }
}