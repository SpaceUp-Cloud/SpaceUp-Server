/*
 * Copyright (c) 2022 Gino Atlas.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package technology.iatlas.spaceup.core.cmd

import io.micronaut.context.annotation.Context
import io.micronaut.tracing.annotation.ContinueSpan
import io.micronaut.tracing.annotation.SpanTag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.services.SshService

@Context
open class Runner<T>(
    private val sshService: SshService) : BaseRunner<T>() {
    private val log: Logger = LoggerFactory.getLogger(Runner::class.java)

    @ContinueSpan
    override suspend fun execute(@SpanTag("runner.cmd") cmd: CommandInf, parser: ParserInf<T>) {
        log.debug("Actual cmd: {} ", cmd.parameters)

        val script = cmd.shellScript
        val output = if(script.name == "") {
            val res = sshService.execute(cmd)
            parser.parseSshOutput(res)
        } else {
            val res = sshService.upload(cmd)
            parser.parseSshOutput(res)
        }

        log.trace("Parsed output: $output")
        subject.onNext(output)
    }
}