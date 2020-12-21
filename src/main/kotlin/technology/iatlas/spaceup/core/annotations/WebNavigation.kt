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
 * @see #RouterService.kt
 * @author Gino Atlas <thraax.session@gino-atlas.de>
 */
annotation class WebNavigation(
    val linkname: String,
    val mapping: String,
    val prio: Int,
    val newTab: Boolean = false
    )
