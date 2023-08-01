package org.hildan.runtimekdoc.reader

import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import org.hildan.runtimekdoc.DOC_RESOURCE_SUFFIX
import org.hildan.runtimekdoc.model.ClassDoc
import org.hildan.runtimekdoc.model.FieldDoc
import org.hildan.runtimekdoc.model.MethodDoc
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.Optional
import kotlin.text.Charsets.UTF_8

/**
 * Allows access to Javadoc elements at runtime. This will only find the Javadoc of elements that were processed by
 * the annotation processor.
 */
object RuntimeJavadoc {

    /**
     * Gets the Javadoc of the given [clazz].
     *
     * @param clazz the class to retrieve the Javadoc for
     *
     * @return the Javadoc of the given class, or an empty optional if no documentation was found
     */
    fun getJavadoc(clazz: Class<*>): Optional<JsonObject> = getJavadoc(clazz.name, clazz)

    /**
     * Gets the Javadoc of the given class, using the given [ClassLoader] to find the Javadoc resource.
     *
     * @param qualifiedClassName the fully qualified name of the class to retrieve the Javadoc for
     * @param classLoader the class loader to use to find the Javadoc resource file
     *
     * @return the Javadoc of the given class, or an empty optional if no documentation was found
     */
    fun getJavadoc(qualifiedClassName: String, classLoader: ClassLoader): Optional<JsonObject> {
        val resourceName = resourceName(qualifiedClassName)
        val inputStream = classLoader.getResourceAsStream(resourceName) ?: return Optional.empty()
        return parseJavadocResource(qualifiedClassName, inputStream)
    }

    /**
     * Gets the Javadoc of the given class, using the given [Class] object to load the Javadoc resource.
     *
     * @param qualifiedClassName the fully qualified name of the class to retrieve the Javadoc for
     * @param loader the class object to use to find the Javadoc resource file
     *
     * @return the Javadoc of the given class, or an empty optional if no documentation was found
     */
    @JvmOverloads
    fun getJavadoc(qualifiedClassName: String, loader: Class<*> = RuntimeJavadoc::class.java): Optional<JsonObject> {
        val resourceName = resourceName(qualifiedClassName)
        val inputStream = loader.getResourceAsStream("/$resourceName") ?: return Optional.empty()
        return parseJavadocResource(qualifiedClassName, inputStream)
    }

    private fun resourceName(qualifiedClassName: String): String =
        qualifiedClassName.replace(".", "/") + DOC_RESOURCE_SUFFIX

    @Throws(IOException::class)
    private fun parseJavadocResource(qualifiedClassName: String, input: InputStream): Optional<JsonObject> {
        InputStreamReader(input, UTF_8).use { reader ->
//            val classDoc = Json.decodeValue(reader.readText(), ClassDoc::class.java)
            return Optional.of(JsonObject(reader.readText()))
        }
    }
//
//    /**
//     * Gets the Javadoc of the given method.
//     *
//     * Implementation note: this method first retrieves the Javadoc of the class, and then matches the method signature
//     * with the correct documentation. If the client code's purpose is to loop through all methods doc, prefer using
//     * [.getJavadoc] (or one of its overloads), and calling [ClassDoc.getMethods] on the
//     * returned class doc to retrieve method docs.
//     *
//     * @param method the method to get the Javadoc for
//     *
//     * @return the given method's Javadoc, or an empty optional if no documentation was found
//     */
//    fun getJavadoc(method: Method): Optional<MethodDoc> =
//        getJavadoc(method.declaringClass).map { it.methods }.flatMap { mDocs -> findMethodJavadoc(mDocs, method) }
//
//    private fun findMethodJavadoc(methodDocs: List<MethodDoc>, method: Method): Optional<MethodDoc> {
//        return Optional.ofNullable(methodDocs.filter { it.matches(method) }.first())
//    }
//
//    /**
//     * Gets the Javadoc of the given field.
//     *
//     * Implementation note: this method first retrieves the Javadoc of the class, and then matches the field name
//     * with the correct documentation. If the client code's purpose is to loop through all fields doc, prefer using
//     * [.getJavadoc] (or one of its overloads), and calling [ClassDoc.getFields] on the
//     * returned class doc to retrieve field docs.
//     *
//     * @param field the field to get the Javadoc for
//     *
//     * @return the given field's Javadoc, or an empty optional if no documentation was found
//     */
//    fun getJavadoc(field: Field): Optional<FieldDoc> =
//        getJavadoc(field.declaringClass).map { it.fields }.flatMap { findFieldJavadoc(it, field) }
//
//    private fun findFieldJavadoc(fieldDocs: List<FieldDoc>, field: Field): Optional<FieldDoc> =
//        Optional.ofNullable(fieldDocs.first { m -> m.name == field.name })
//
//    /**
//     * Gets the Javadoc of the given enum constant.
//     *
//     * Implementation note: this method first retrieves the Javadoc of the class, and then matches the enum constant's
//     * name with the correct documentation. If the client code's purpose is to loop through all enum constants docs,
//     * prefer using [.getJavadoc] (or one of its overloads), and calling
//     * [ClassDoc.getEnumConstants] on the returned class doc to retrieve enum constant docs.
//     *
//     * @param enumValue the enum constant to get the Javadoc for
//     *
//     * @return the given enum constant's Javadoc, or an empty optional if no documentation was found
//     */
//    fun getJavadoc(enumValue: Enum<*>): Optional<FieldDoc> =
//        getJavadoc(enumValue.javaClass).map { it.enumConstants }.flatMap { findEnumValueJavadoc(it, enumValue) }
//
//    private fun findEnumValueJavadoc(fieldDocs: List<FieldDoc>, enumValue: Enum<*>): Optional<FieldDoc> =
//        Optional.ofNullable(fieldDocs.first { it.name == enumValue.name })
}
