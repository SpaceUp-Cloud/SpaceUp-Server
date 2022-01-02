/*
 * Copyright (c) 2022 Gino Atlas.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package technology.iatlas.spaceup.core.annotations

/**
 * This is an extra annotation which links every @Controller annotation path the link name.
 * It helps to generate automatically a navigation panel on client side.
 * The implementation is in the RouterService.kt
 *
 * @param linkname  The client link name for mapping route and name to the navigation
 * @param mapping The url which we map to the name
 * @param prio Introduces the order in the web navigation
 * @param newTab If the link should be opened in a new tab or target self
 * @since 10.12.2021
 * @see ***REMOVED***RouterService.kt
 * @author Gino Atlas <thraax.session@gino-atlas.de>
 */
annotation class WebNavigation(
    val linkname: String,
    val mapping: String,
    val prio: Int,
    val newTab: Boolean = false
    )
