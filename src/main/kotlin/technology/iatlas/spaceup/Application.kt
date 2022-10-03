/*
 * Copyright (c) 2022 Gino Atlas.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package technology.iatlas.spaceup

import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyExchange
import com.jcraft.jsch.Session
import com.jcraft.jsch.jce.AES128CTR
import com.jcraft.jsch.jce.DH
import com.jcraft.jsch.jce.HMACSHA1
import com.jcraft.jsch.jce.HMACSHA256ETM
import com.jcraft.jsch.jce.Random
import com.jcraft.jsch.jce.SHA256
import com.jcraft.jsch.jce.SignatureRSASHA512
import io.micronaut.core.annotation.TypeHint
import io.micronaut.http.HttpResponse
import io.micronaut.runtime.Micronaut.build
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import org.jasypt.normalization.Normalizer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.dto.Feedback
import java.text.Normalizer.Form
import java.text.Normalizer as jNormalizer


@OpenAPIDefinition(
    info = Info(
        title = "SpaceUp",
        version = "0.26.0",
        contact = Contact(name = "Thraax Session",
            url = "https://spaceup.iatlas.technology", email = "spaceup@iatlas.technology")
    )
)
@TypeHint(
    value = [
        LoggerFactory::class,
        Logger::class,
        com.jcraft.jsch.Logger::class,
        jNormalizer::class,
        Normalizer::class,
        Form::class,
        StandardPBEStringEncryptor::class,
        StandardPBEByteEncryptor::class,
        Random::class,
        JSch::class,
        Channel::class,
        ChannelExec::class,
        ChannelSftp::class,
        Session::class,
        HMACSHA1::class,
        AES128CTR::class,
        KeyExchange::class,
        SHA256::class,
        DH::class,
        SignatureRSASHA512::class,
        HMACSHA256ETM::class,
    ],
    accessType = [
        TypeHint.AccessType.ALL_DECLARED_CONSTRUCTORS,
        TypeHint.AccessType.ALL_PUBLIC,
        TypeHint.AccessType.ALL_DECLARED_FIELDS,
        TypeHint.AccessType.ALL_DECLARED_METHODS,
        TypeHint.AccessType.ALL_PUBLIC_CONSTRUCTORS,
        TypeHint.AccessType.ALL_PUBLIC_FIELDS,
        TypeHint.AccessType.ALL_PUBLIC_METHODS,
    ],
    typeNames = [
        "com.jcraft.jsch.DHGEX256",
        "com.jcraft.jsch.UserAuthNone",
        "com.jcraft.jsch.UserAuthPassword",
        "com.jcraft.jsch.UserAuthPublicKey",
        "com.jcraft.jsch.UserAuthKeyboardInteractive",
    ]
)
object Api

fun main(args: Array<String>) {
	build()
        .eagerInitSingletons(true)
	    .args(*args)
		.packages("technology.iatlas.spaceup")
        .defaultEnvironments("prod")
		.start()
}

fun String.encrypt(secret: String): String {
    val passwordEncryptor = StandardPBEStringEncryptor()
    passwordEncryptor.setPassword(secret)
    return passwordEncryptor.encrypt(this)
}

fun String.decrypt(secret: String): String {
    val passwordEncryptor = StandardPBEStringEncryptor()
    passwordEncryptor.setPassword(secret)
    return passwordEncryptor.decrypt(this)
}

fun Feedback.toHttpResponse(): HttpResponse<Feedback> {
    return if(this.isOk()) {
        HttpResponse.created(this)
    } else {
        HttpResponse.badRequest(this)
    }
}

fun Feedback.isOk(): Boolean {
    return if((this.info.isNotEmpty() && this.error.isEmpty()) || this.error.isEmpty()) {
        true
    } else if (this.error.isNotEmpty()) {
        false
    } else {
        false
    }
}