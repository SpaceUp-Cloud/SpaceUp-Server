package technology.iatlas.spaceup.core.annotations

import io.micronaut.aop.Around
import io.micronaut.context.annotation.Bean
import kotlin.annotation.AnnotationTarget.*

@Around
@Bean
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(CLASS, FILE, ANNOTATION_CLASS, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
annotation class Installed {
}