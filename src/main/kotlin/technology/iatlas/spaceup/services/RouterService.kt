package technology.iatlas.spaceup.services

import io.micronaut.cache.annotation.Cacheable
import io.micronaut.http.annotation.Controller
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.annotations.ClientLink
import java.io.File
import java.net.URI
import java.util.*
import javax.inject.Singleton
import kotlin.reflect.jvm.reflect

@Singleton
open class RouterService {

    private val log = LoggerFactory.getLogger(RouterService::class.java)

    /**
     * Linking between controller path and navigation name
     */
    @Cacheable("client-controller-link")
    open fun getClientControllerLinking(packageName: String = "technology.iatlas.spaceup"): Map<String, String> {
        val mapping = mutableMapOf<String, String>()

        getRoutes(packageName).filter {
            it.getAnnotation(Controller::class.java) != null &&
                    it.getAnnotation(ClientLink::class.java) != null
        }.forEach {
            val name = it.getAnnotation(ClientLink::class.java).name
            val uri = it.getAnnotation(Controller::class.java).value
            mapping[name] = uri

            log.trace("Found Route: {} to {}", name, uri)
        }

        return mapping
    }

    private fun getRoutes(givenPackage: String): List<Class<*>> {
        /*
        TODO: put all in cache as this is only necessary after init
         */

        val classLoader = Thread.currentThread().contextClassLoader
        val path: String = givenPackage.replace('.', '/')
        val resources = classLoader.getResources(path)
        val dirs: MutableList<File> = ArrayList()
        while (resources.hasMoreElements()) {
            val resource = resources.nextElement()
            val uri = URI(resource.toString())
            dirs.add(File(uri.path))
        }
        val classes: MutableList<Class<*>> = ArrayList()
        for (directory in dirs) {
            classes.addAll(findClasses(directory, givenPackage))
        }

        return classes
    }

    /**
     * Recursive method used to find all classes in a given directory and
     * subdirs.
     *
     * @param directory
     * The base directory
     * @param packageName
     * The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    @Throws(ClassNotFoundException::class)
    private fun findClasses(directory: File, packageName: String): List<Class<*>> {
        val classes: MutableList<Class<*>> = ArrayList()
        if (!directory.exists()) {
            return classes
        }
        val files: Array<File>? = directory.listFiles()

        if (files != null) {
            for (file in files) {
                if (file.isDirectory) {
                    classes.addAll(findClasses(file, packageName + "." + file.name))
                } else if (file.name.endsWith(".class")) {
                    classes.add(Class.forName(packageName + '.' + file.name.substring(0, file.name.length - 6)))
                }
            }
        }
        return classes
    }

}