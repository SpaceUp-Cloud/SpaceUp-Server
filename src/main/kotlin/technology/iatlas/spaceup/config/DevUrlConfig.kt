package technology.iatlas.spaceup.config

import io.micronaut.context.annotation.EachProperty
import io.micronaut.context.annotation.Parameter
import io.micronaut.core.annotation.Introspected

@Introspected
@EachProperty("devurl")
class DevUrlConfig constructor(@param:Parameter val name: String) {
    var enabled: Boolean? = null
    var mapping: String? = null
    var newtab: Boolean? = null
}
