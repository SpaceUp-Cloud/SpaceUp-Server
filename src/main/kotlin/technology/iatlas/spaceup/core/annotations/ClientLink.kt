package technology.iatlas.spaceup.core.annotations

/**
 * This is an extra annotation which links every @Controller annotation path the link name.
 * It helps to generate automatically a navigation panel on client side.
 * The implementation is in the RouterService.kt
 *
 * @param name  The client link name for mapping route and name to the navigation
 * @since 10.12.2021
 * @see #RouterService.kt
 * @author Gino Atlas <thraax.session@gino-atlas.de>
 */
annotation class ClientLink(val name: String)
