/*
 * Copyright (c) 2022 Thraax Session <spaceup@iatlas.technology>.
 *
 * SpaceUp-Server is free software; You can redistribute it and/or modify it under the terms of:
 *   - the GNU Affero General Public License version 3 as published by the Free Software Foundation.
 * You don't have to do anything special to accept the license and you don’t have to notify anyone which that you have made that decision.
 *
 * SpaceUp-Server is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See your chosen license for more details.
 *
 * You should have received a copy of both licenses along with SpaceUp-Server
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * There is a strong belief within us that the license we have chosen provides not only the best solution for providing you with the essential freedom necessary to use SpaceUp-Server within your projects, but also for maintaining enough copyleft strength for us to feel confident and secure with releasing our hard work to the public. For your convenience we've included our own interpretation of the license we chose, which can be seen below.
 *
 * Our interpretation of the GNU Affero General Public License version 3: (Quoted words are words in which there exists a definition within the license to avoid ambiguity.)
 *   1. You must always provide the source code, copyright and license information of SpaceUp-Server whenever you "convey" any part of SpaceUp-Server;
 *      be it a verbatim copy or a modified copy.
 *   2. SpaceUp-Server was developed as a library and has therefore been designed without knowledge of your work; as such the following should be implied:
 *      a) SpaceUp-Server was developed without knowledge of your work; as such the following should be implied:
 *         i)  SpaceUp-Server should not fall under a work which is "based on" your work.
 *         ii) You should be free to use SpaceUp-Server in a work covered by the:
 *             - GNU General Public License version 2
 *             - GNU Lesser General Public License version 2.1
 *             This is due to those licenses classifying SpaceUp-Server as a work which would fall under an "aggregate" work by their terms and definitions;
 *             as such it should not be covered by their terms and conditions. The relevant passages start at:
 *             - Line 129 of the GNU General Public License version 2
 *             - Line 206 of the GNU Lesser General Public License version 2.1
 *      b) If you have not "modified", "adapted" or "extended" SpaceUp-Server then your work should not be bound by this license,
 *         as you are using SpaceUp-Server under the definition of an "aggregate" work.
 *      c) If you have "modified", "adapted" or "extended" SpaceUp-Server then any of those modifications/extensions/adaptations which you have made
 *         should indeed be bound by this license, as you are using SpaceUp-Server under the definition of a "based on" work.
 *
 * Our hopes is that our own interpretation of license aligns perfectly with your own values and goals for using our work freely and securely. If you have any questions at all about the licensing chosen for SpaceUp-Server you can email us directly at spaceup@iatlas.technology or you can get in touch with the license authors (the Free Software Foundation) at licensing@fsf.org to gain their opinion too.
 *
 * Alternatively you can provide feedback and acquire the support you need at our support forum. We'll definitely try and help you as soon as possible, and to the best of our ability; as we understand that user experience is everything, so we want to make you as happy as possible! So feel free to get in touch via our support forum and chat with other users of SpaceUp-Server here at:
 * https://spaceup.iatlas.technology
 *
 * Thanks, and we hope you enjoy using SpaceUp-Server and that it's everything you ever hoped it could be.
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
@OpenAPIDefinition(
    info = Info(
        title = "SpaceUp",
        version = "0.26.0",
        contact = Contact(name = "Thraax Session",
            url = "https://spaceup.iatlas.technology", email = "spaceup@iatlas.technology")
    )
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
        HttpResponse.ok(this)
    } else {
        HttpResponse.badRequest(this)
    }
}

fun Feedback.isOk(): Boolean {
    return if(this.info.isNotEmpty() && this.error.isEmpty() || (this.info.isEmpty() && this.error.isEmpty())) {
        true
    } else if (this.error.isNotEmpty()) {
        false
    } else {
        false
    }
}