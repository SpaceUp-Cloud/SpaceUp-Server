/*
 * Copyright (c) 2022 Gino Atlas.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package technology.iatlas.spaceup.controller

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import org.asciidoctor.*
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.Path

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/")
class DocController(
    private val es: EmbeddedServer
) {
    private val log = LoggerFactory.getLogger(DocController::class.java)
    private val stylesDir = this.javaClass.getResource("/docs/asciidoc/style")!!

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Get(uri = "{?theme}", produces = [MediaType.TEXT_HTML])
    fun getDocs(theme: Optional<String>): String {
        val baseUrl = es.scheme + "://" + es.host + ":" + es.port

        val applyTheme = if(theme.isPresent) theme.get() else "asciidoctor"

        val adoc = this.javaClass.getResource("/docs/asciidoc/index.adoc")!!.readText()
        val cssDir = File(stylesDir.file).canonicalPath
        log.debug("CSS dir: $cssDir")

        System.setProperty("jruby.compat.version", "RUBY1_9")
        System.setProperty("jruby.compile.mode", "OFF")

        val asciidoc = Asciidoctor.Factory.create()
        asciidoc.requireLibrary("asciidoctor-diagram")
        val attr = Attributes.builder()
            .styleSheetName("$applyTheme.css")
            .stylesDir(cssDir)
            .linkCss(false)
            .copyCss(false)
            .tableOfContents(true)
            .tableOfContents(Placement.LEFT)
            .dataUri(true)
            .allowUriRead(true)
            .experimental(true)

            .attribute("data-baseurl", baseUrl)
            .attribute("data-themes", getAllStylesheets())

            .build()

        val options = Options.builder()
            .safe(SafeMode.SAFE)
            .headerFooter(true)
            .backend("html5")
            .build()
        options.setAttributes(attr)

        return asciidoc.convert(adoc, options)
    }

    private fun getAllStylesheets(): List<String> {
        val allFiles = mutableListOf<String>()

        /*File(stylesDir.file).walk().forEach {
            log.debug("CSS file: $it")
            if(it.isFile && it.name.contains(".css")) {
                allFiles.add(it.name.split(".")[0])
            }
        }*/

        val absPath = Path(stylesDir.path)
        Files.walk(absPath).forEach {
            if(it.fileName.endsWith(".css")) {
                log.debug("CSS file: $it")
                allFiles.add(it.fileName.toString().split(".")[0])
            }
        }

        return allFiles.sorted()
    }

}
