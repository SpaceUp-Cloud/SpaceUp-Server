/*
 * Copyright (c) 2022 spaceup@iatlas.technology.
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
 *
 * There is a strong belief within us that the license we have chosen provides not only the best solution for providing you with the essential freedom necessary to use <project name> within your projects, but also for maintaining enough copyleft strength for us to feel confident and secure with releasing our hard work to the public. For your convenience we've included our own interpretation of the license we chose, which can be seen below.
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
 *             This is due to those licenses classifying <project name> as a work which would fall under an "aggregate" work by their terms and definitions;
 *             as such it should not be covered by their terms and conditions. The relevant passages start at:
 *             - Line 129 of the GNU General Public License version 2
 *             - Line 206 of the GNU Lesser General Public License version 2.1
 *      b) If you have not "modified", "adapted" or "extended" SpaceUp-Server then your work should not be bound by this license,
 *         as you are using <project name> under the definition of an "aggregate" work.
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

package technology.iatlas.spaceup.core.helper
/**
 * Credits to https://github.com/marcelmatula/colored-console
 */

import technology.iatlas.spaceup.core.helper.ColoredConsole.Companion.BLACK
import technology.iatlas.spaceup.core.helper.ColoredConsole.Companion.BRIGHT_BLACK
import technology.iatlas.spaceup.core.helper.ColoredConsole.Companion.BRIGHT_WHITE
import technology.iatlas.spaceup.core.helper.ColoredConsole.Companion.RESET
import technology.iatlas.spaceup.core.helper.ColoredConsole.Companion.WHITE
import technology.iatlas.spaceup.core.helper.ColoredConsole.Style
import technology.iatlas.spaceup.core.helper.ColoredConsole.Style.NotApplied
import java.util.regex.Pattern

interface ColoredConsole {

    sealed class Style {

        val bg: Style get() = when (this){
            is Simple -> if (code.isColor) copy(code = code + BACKGROUND_SHIFT) else this
            is Composite -> if (parent is Simple && parent.code.isColor)
                copy(parent = parent.copy(code = parent.code + BACKGROUND_SHIFT))
            else this
            is NotApplied -> this
        }

        val bright: Style get() = when (this){
            is Simple -> if (code.isNormalColor) copy(code = code + BRIGHT_SHIFT) else this
            is Composite -> if (parent is Simple && parent.code.isNormalColor)
                copy(parent = parent.copy(code = parent.code + BRIGHT_SHIFT))
            else this
            is NotApplied -> this
        }

        abstract fun wrap(text: String): String

        object NotApplied : Style() {
            override fun wrap(text: String) = text
        }

        data class Simple(val code: Int) : Style() {
            override fun wrap(text: String) = text.applyCodes(code)
        }

        data class Composite(val parent: Style, private val child: Style) : Style() {
            override fun wrap(text: String) = parent.wrap(child.wrap(text))
        }

        operator fun plus(style: Style) = when (this) {
            is NotApplied -> this
            is Simple -> Composite(style, this)
            is Composite -> Composite(style, this)
        }
    }

    fun <N> N.style(style: Style, predicate: (N) -> Boolean = { true }) =
        takeIf { predicate(this) }?.let { style.wrap(toString()) } ?: toString()

    operator fun <N> N.invoke(style: Style, predicate: (N) -> Boolean = { true }) = style(style, predicate)

    fun <N> N.wrap(vararg ansiCodes: Int) = toString().let { text ->
        if (this@ColoredConsole is ColorConsoleDisabled)
            text
        else {
            val codes = ansiCodes.filter { it != RESET }
            text.applyCodes(*codes.toIntArray())
        }
    }

    private val String.firstAnsi get() = pattern.matcher(this).let { matcher ->
        if (!matcher.find() || matcher.start() != 0) null else matcher.group(1).toIntOrNull()
    }

    val String.bright get() = firstAnsi.let { code ->
        if (code?.isNormalColor == true) substring(0, 2) + (code + BRIGHT_SHIFT) + substring(4) else this
    }

    val String.bg get() = firstAnsi.let { code ->
        if (code?.isColor == true) substring(0, 2) + (code + BACKGROUND_SHIFT) + substring(4) else this
    }

    // region styles
    val bold: Style get() = Style.Simple(HIGH_INTENSITY)
    val <N : Style> N.bold: Style get() = this + this@ColoredConsole.bold
    val <N> N.bold get() = wrap(HIGH_INTENSITY)
    fun <N> N.bold(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.bold?: this.toString()
    fun bold(text: Any) = text.wrap(HIGH_INTENSITY)

    val faint: Style get() = Style.Simple(LOW_INTENSITY)
    val <N : Style> N.faint: Style get() = this + this@ColoredConsole.faint
    val <N> N.faint get() = wrap(LOW_INTENSITY)
    fun <N> N.faint(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.faint?: this.toString()
    fun faint(text: Any) = text.wrap(LOW_INTENSITY)

    val italic: Style get() = Style.Simple(ITALIC)
    val <N : Style> N.italic: Style get() = this + this@ColoredConsole.italic
    val <N> N.italic get() = wrap(ITALIC)
    fun <N> N.italic(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.italic?: this.toString()
    fun italic(text: String) = text.wrap(ITALIC)

    val underline: Style get() = Style.Simple(UNDERLINE)
    val <N : Style> N.underline: Style get() = this + this@ColoredConsole.underline
    val <N> N.underline get() = wrap(UNDERLINE)
    fun <N> N.underline(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.underline?: this.toString()
    fun underline(text: String) = text.wrap(UNDERLINE)

    val blink: Style get() = Style.Simple(BLINK)
    val <N : Style> N.blink: Style get() = this + this@ColoredConsole.blink
    val <N> N.blink get() = wrap(BLINK)
    fun <N> N.blink(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.blink?: this.toString()
    fun blink(text: String) = text.wrap(BLINK)

    val reverse: Style get() = Style.Simple(REVERSE)
    val <N : Style> N.reverse: Style get() = this + this@ColoredConsole.reverse
    val <N> N.reverse get() = wrap(REVERSE)
    fun <N> N.reverse(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.reverse?: this.toString()
    fun reverse(text: String) = text.wrap(REVERSE)

    val hidden: Style get() = Style.Simple(HIDDEN)
    val <N : Style> N.hidden: Style get() = this + this@ColoredConsole.hidden
    val <N> N.hidden get() = wrap(HIDDEN)
    fun <N> N.hidden(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.hidden?: this.toString()
    fun hidden(text: String) = text.wrap(HIDDEN)

    val strike: Style get() = Style.Simple(STRIKE)
    val <N : Style> N.strike: Style get() = this + this@ColoredConsole.strike
    val <N> N.strike get() = wrap(STRIKE)
    fun <N> N.strike(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.strike?: this.toString()
    fun strike(text: String) = text.wrap(STRIKE)
    // endregion

    // region colors
    val black: Style get() = Style.Simple(BLACK)
    val <N : Style> N.black: Style get() = this + this@ColoredConsole.black
    val <N> N.black get() = wrap(BLACK)
    fun <N> N.black(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.black?: toString()
    fun black(text: String) = text.wrap(BLACK)

    val red: Style get() = Style.Simple(RED)
    val <N : Style> N.red: Style get() = this + this@ColoredConsole.red
    val <N> N.red get() = wrap(RED)
    fun <N> N.red(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.red?: toString()
    fun red(text: String) = text.wrap(RED)

    val green: Style get() = Style.Simple(GREEN)
    val <N : Style> N.green: Style get() = this + this@ColoredConsole.green
    val <N> N.green get() = wrap(GREEN)
    fun <N> N.green(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.green?: toString()
    fun green(text: String) = text.wrap(GREEN)

    val yellow: Style get() = Style.Simple(YELLOW)
    val <N : Style> N.yellow: Style get() = this + this@ColoredConsole.yellow
    val <N> N.yellow get() = wrap(YELLOW)
    fun <N> N.yellow(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.yellow?: toString()
    fun yellow(text: String) = text.wrap(YELLOW)

    val blue: Style get() = Style.Simple(BLUE)
    val <N : Style> N.blue: Style get() = this + this@ColoredConsole.blue
    val <N> N.blue get() = wrap(BLUE)
    fun <N> N.blue(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.blue?: toString()
    fun blue(text: String) = text.wrap(BLUE)

    val purple: Style get() = Style.Simple(PURPLE)
    val <N : Style> N.purple: Style get() = this + this@ColoredConsole.purple
    val <N> N.purple get() = wrap(PURPLE)
    fun <N> N.purple(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.purple?: toString()
    fun purple(text: String) = text.wrap(PURPLE)

    val cyan: Style get() = Style.Simple(CYAN)
    val <N : Style> N.cyan: Style get() = this + this@ColoredConsole.cyan
    val <N> N.cyan get() = wrap(CYAN)
    fun <N> N.cyan(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.cyan?: toString()
    fun cyan(text: String) = text.wrap(CYAN)

    val white: Style get() = Style.Simple(WHITE)
    val <N : Style> N.white: Style get() = this + this@ColoredConsole.white
    val <N> N.white get() = wrap(WHITE)
    fun <N> N.white(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.white?: toString()
    fun white(text: String) = text.wrap(WHITE)
    // endregion

    companion object {
        const val RESET = 0

        const val HIGH_INTENSITY = 1
        const val LOW_INTENSITY = 2

        const val BACKGROUND_SHIFT = 10
        const val BRIGHT_SHIFT = 60

        const val ITALIC = 3
        const val UNDERLINE = 4
        const val BLINK = 5
        const val REVERSE = 7
        const val HIDDEN = 8
        const val STRIKE = 9

        const val BLACK = 30
        const val RED = 31
        const val GREEN = 32
        const val YELLOW = 33
        const val BLUE = 34
        const val PURPLE = 35
        const val CYAN = 36
        const val WHITE = 37

        const val BRIGHT_BLACK = BLACK + BRIGHT_SHIFT
        const val BRIGHT_RED = RED + BRIGHT_SHIFT
        const val BRIGHT_GREEN = GREEN + BRIGHT_SHIFT
        const val BRIGHT_YELLOW = YELLOW + BRIGHT_SHIFT
        const val BRIGHT_BLUE = BLUE + BRIGHT_SHIFT
        const val BRIGHT_PURPLE = PURPLE + BRIGHT_SHIFT
        const val BRIGHT_CYAN = CYAN + BRIGHT_SHIFT
        const val BRIGHT_WHITE = WHITE + BRIGHT_SHIFT

        val pattern: Pattern = Pattern.compile("\\u001B\\[([0-9]{1,2})m")
    }
}

private interface ColorConsoleDisabled : ColoredConsole {

    override val bold get() = NotApplied
    override val <N : Style> N.bold: Style get() = NotApplied
    override val italic get() = NotApplied
    override val <N : Style> N.italic: Style get() = NotApplied
    override val underline get() = NotApplied
    override val <N : Style> N.underline: Style get() = NotApplied
    override val blink get() = NotApplied
    override val <N : Style> N.blink: Style get() = NotApplied
    override val reverse get() = NotApplied
    override val <N : Style> N.reverse: Style get() = NotApplied
    override val hidden get() = NotApplied
    override val <N : Style> N.hidden: Style get() = NotApplied

    override val red get() = NotApplied
    override val <N : Style> N.red: Style get() = NotApplied
    override val black get() = NotApplied
    override val <N : Style> N.black: Style get() = NotApplied
    override val green get() = NotApplied
    override val <N : Style> N.green: Style get() = NotApplied
    override val yellow get() = NotApplied
    override val <N : Style> N.yellow: Style get() = NotApplied
    override val blue get() = NotApplied
    override val <N : Style> N.blue: Style get() = NotApplied
    override val purple get() = NotApplied
    override val <N : Style> N.purple: Style get() = NotApplied
    override val cyan get() = NotApplied
    override val <N : Style> N.cyan: Style get() = NotApplied
    override val white get() = NotApplied
    override val <N : Style> N.white: Style get() = NotApplied
}

private val Int.isNormalColor get() = this in BLACK..WHITE
private val Int.isBrightColor get() = this in BRIGHT_BLACK..BRIGHT_WHITE
private val Int.isColor get() = isNormalColor || isBrightColor

private fun String.applyCodes(vararg codes: Int) = "\u001B[${RESET}m".let { reset ->
    val tags = codes.joinToString { "\u001B[${it}m" }
    split(reset).filter { it.isNotEmpty() }.joinToString(separator = "") { tags + it + reset }
}

//@UseExperimental(ExperimentalContracts::class)
fun <R> colored(enabled: Boolean = true, block: ColoredConsole.() -> R): R {
//    contract {
//        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
//    }
    check(true)
    return if (enabled) object : ColoredConsole {}.block() else object : ColorConsoleDisabled {}.block()
}

fun <R : Style> style(block: ColoredConsole.() -> R): R = object : ColoredConsole {}.block()

fun print(colored: Boolean = true, block: ColoredConsole.() -> String) = colored(colored) { print(block()) }
fun println(colored: Boolean = true, block: ColoredConsole.() -> String) = colored(colored) { println(block()) }