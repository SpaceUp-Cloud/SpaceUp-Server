package technology.iatlas.spaceup.services

import io.micronaut.cache.annotation.Cacheable
import io.micronaut.context.env.Environment
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.tracing.annotation.ContinueSpan
import jdk.internal.org.objectweb.asm.ClassReader
import jdk.internal.org.objectweb.asm.ClassVisitor
import jdk.internal.org.objectweb.asm.Opcodes
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.annotations.ClientLink
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import java.util.jar.JarFile
import javax.inject.Singleton


@Singleton
open class RouterService(private val env: Environment) {

    private val log = LoggerFactory.getLogger(RouterService::class.java)

    /**
     * Linking between controller path and navigation name
     */
    @Cacheable("client-controller-link")
    @ContinueSpan
    open fun createControllerNameLink(): Map<String, String> {
        val mapping = mutableMapOf<String, String>()

        if (env.activeNames.contains("dev")) {
            mapping["Swagger"] = "/swagger-ui"

            log.warn("Added developer links to navigation")
        }

        getClasses()?.filter {
            it.isAnnotationPresent(Controller::class.java) &&
                    it.methods.any { m ->
                        m.isAnnotationPresent(ClientLink::class.java) &&
                                m.isAnnotationPresent(Get::class.java)
                    }
        }?.forEach {
            val controller = it.getAnnotation(Controller::class.java)
            val controllerUri = controller.value

            // Iterate over "Get" annotations
            val methods =
                it.methods.toList()

            methods.filter { method ->
                method.isAnnotationPresent(ClientLink::class.java) &&
                        method.isAnnotationPresent(Get::class.java)
            }.forEach { method ->
                // Check if method has necessary annotations
                val getUri = method.getAnnotation(Get::class.java)
                val clientLink = method.getAnnotation(ClientLink::class.java)
                val clientLinkName = clientLink.name

                if (getUri.uri.isNotEmpty() && getUri.uri != "/") {
                    /*
                        Case:
                        - Getter has Uri
                    */
                    val actualUri = controllerUri + getUri.uri
                    log.debug("Found Link: {} to {}", clientLink, actualUri)
                    mapping[clientLinkName] = actualUri
                } else {
                    /*
                        Case:
                        - Getter does not have Uri
                    */
                    log.debug("Found Link: {} to {}", clientLink, controllerUri)
                    mapping[clientLinkName] = controllerUri
                }
            }
        }
        return mapping
    }

    private fun getClassesNew(givenPackage: String = "technology.iatlas.spaceup.controller"): List<Class<*>> {
        /*
        TODO: put all in cache as this is only necessary after init
         */


        //val listOfClasses = mutableListOf<Class<*>>()

        val classes = this::class.java.classes
        val listOfClasses = classes.toList()//.filter {
            //it.`package`.name.contains(givenPackage)
        //}
        listOfClasses.forEach { println(it.name) }

        // In PROD we have a list of JARs in classpath
        /*val result: MutableList<URL> = ArrayList()

        //var cl = Thread.currentThread().contextClassLoader
        var cl = this::class.java.classLoader
        while (cl != null) {
            if (cl is URLClassLoader) {
                val urls = cl.urLs
                result.addAll(listOf(*urls))
            }
            cl = cl.parent
        }

        for (url in getRootUrls()!!) {
            val f = File(url.path)
            if (f.isDirectory) {
                visitFile(f, listOfClasses)
            } else {
                val jarName = File(
                    this::class.java
                        .protectionDomain.codeSource.location.toURI()
                );
                if (url.file.contains(jarName.name)) {
                    visitJar(url, listOfClasses)
                }
            }
        }*/

        return listOfClasses
    }

    /**
     * Scans all classes accessible from the context class loader which belong
     * to the given package and subpackages.
     *
     * @param packageName
     * The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    @Throws(ClassNotFoundException::class, IOException::class)
    private fun getClasses(packageName: String = "technology.iatlas.spaceup.controller"): Iterable<Class<*>>? {
        val classes: MutableList<Class<*>> = ArrayList()

        val classLoader = Thread.currentThread().contextClassLoader
        val path = packageName.replace('.', '/')
        val resources = classLoader.getResources(path)
        val dirs: MutableList<File> = ArrayList()
        while (resources.hasMoreElements()) {
            val resource = resources.nextElement()
            val uri = URI(resource.toString())
            if(uri.path != null) {
                dirs.add(File(uri.path))
            } else {
                visitJar(uri.toURL(), classes)
            }
        }

        for (directory in dirs) {
            classes.addAll(findClasses(directory, packageName))
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
        val files = directory.listFiles()?.toList()
        files?.forEach { file ->
            if (file.isDirectory) {
                classes.addAll(findClasses(file, packageName + "." + file.name))
            } else if (file.name.endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.name.substring(0, file.name.length - 6)))
            }
        }
        return classes
    }
}



fun getRootUrls(): List<URL>? {
    val result: MutableList<URL> = ArrayList()
    var cl = Thread.currentThread().contextClassLoader
    while (cl != null) {
        if (cl is URLClassLoader) {
            val urls = cl.urLs
            result.addAll(listOf(*urls))
        }
        cl = cl.parent
    }
    return result
}


@Throws(IOException::class)
fun visitFile(f: File, l: MutableList<Class<*>>) {
    if (f.isDirectory) {
        val children = f.listFiles()
        if (children != null) {
            for (child in children) {
                visitFile(child, l)
            }
        }
    } else if (f.name.endsWith(".class")) {
        //FileInputStream(f).use { }
        FileInputStream(f).use {
            handleClass(it, l)
        }
    }
}

@Throws(IOException::class)
fun visitJar(url: URL, l: MutableList<Class<*>>) {
    val jarFile = JarFile(url.file)
    val jarEntries = jarFile.entries()
    while (jarEntries.hasMoreElements()) {
        val je = jarEntries.nextElement()
        if (!je.isDirectory && je.name.endsWith(".class")) {//&& je.getName().startsWith(jfp)) {
            val fileInJar = je.name
            if (fileInJar.contains("spaceup") && !fileInJar.contains("$")) {
                //val filePath = File(url.toURI().path + "/" + je.name)
                val filePath = File(je.name)
                println(filePath)

                FileInputStream(filePath).use {
                    handleClass(it, l)
                }
            }
        }


        /*url.openStream().use { urlIn ->
        JarInputStream(urlIn).use { jarIn ->
            while (jarIn.nextJarEntry != null) {
                val entry = jarIn.nextJarEntry
                if (entry != null && entry.name.endsWith(".class")) {
                    val className = entry.name
                    val myClass = className.substring(0, className.lastIndexOf('.'))
                    try {
                        FileInputStream(myClass).use {
                            handleClass(it, l)
                        }
                    }catch (e: FileNotFoundException) {
                        // ignore
                    }
                }
            }
        }
    }*/
    }
}

@Throws(IOException::class)
fun handleClass(`in`: InputStream?, list: MutableList<Class<*>>) {
    val cv = MyClassVisitor()
    ClassReader(`in`).accept(cv, 0)
    if (cv.hasSuffix && cv.hasAnnotation) {
        // Class with annotation
        val clazz = Class.forName(cv.className)
        list.add(clazz)
    }
}

internal class MyClassVisitor : ClassVisitor(Opcodes.ASM5) {
    var hasSuffix = false
    var hasAnnotation = false
    var className: String? = null
    override fun visit(
        version: Int,
        access: Int, name: String, signature: String?,
        superName: String?, interfaces: Array<String?>?
    ) {
        className = name.replace('/', '.')
        hasSuffix = name.endsWith("Controller")
    }
}
