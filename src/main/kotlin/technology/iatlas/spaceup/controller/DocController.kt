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
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import org.asciidoctor.*
import java.util.*

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/")
class DocController {

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Get(uri = "{?theme}", produces = [MediaType.TEXT_HTML])
    fun getDocs(theme: Optional<String>): String {
        val applyTheme = if(theme.isPresent) theme.get() else "asciidoctor"

        val adoc = this.javaClass.getResource("/docs/asciidoc/index.adoc")!!.readText()

        System.setProperty("jruby.compat.version", "RUBY1_9")
        System.setProperty("jruby.compile.mode", "OFF")

        val asciidoc = Asciidoctor.Factory.create()
        asciidoc.requireLibrary("asciidoctor-diagram")
        val attr = Attributes.builder()
            .styleSheetName("$applyTheme.css")
            .linkCss(true)
            .tableOfContents(true)
            .tableOfContents(Placement.LEFT)
            .dataUri(true)
            .experimental(true)
            .build()

        val options = Options.builder()
            .safe(SafeMode.SAFE)
            .headerFooter(true)
            .backend("html5")
            .build()
        options.setAttributes(attr)

        return asciidoc.convert(adoc, options)
    }

}
