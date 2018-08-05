package org.hildan.runtimekdoc.model

import java.lang.reflect.Method

data class ClassDoc(
    val name: String,
    val fields: List<FieldDoc>,
    val enumConstants: List<FieldDoc>,
    val methods: List<MethodDoc>,
    val description: Comment?,
    val seeAlso: List<SeeAlsoTag>,
    val other: List<JavadocTag>
) {
    constructor(
        name: String,
        fields: List<FieldDoc>,
        enumConstants: List<FieldDoc>,
        methods: List<MethodDoc>,
        javadoc: JavadocComment
    ) : this(name, fields, enumConstants, methods, javadoc.description, javadoc.seeAlso, javadoc.other)
}

data class FieldDoc(
    val name: String,
    val description: Comment?,
    val seeAlso: List<SeeAlsoTag>,
    val other: List<JavadocTag>
) {
    constructor(name: String, javadoc: JavadocComment) : this(name, javadoc.description, javadoc.seeAlso, javadoc.other)
}

data class MethodDoc(
    val name: String,
    val paramTypes: List<String>,
    val description: Comment?,
    val params: List<ParamTag>,
    val returns: Comment?,
    val throws: List<ThrowsTag>,
    val seeAlso: List<SeeAlsoTag>,
    val other: List<JavadocTag>
) {
    constructor(name: String, paramTypes: List<String>, javadoc: JavadocComment) :
            this(
                name,
                paramTypes,
                javadoc.description,
                javadoc.params,
                javadoc.returns,
                javadoc.throws,
                javadoc.seeAlso,
                javadoc.other
            )

    fun matches(method: Method): Boolean {
        if (method.name != name) {
            return false
        }
        val methodParamsTypes = method.parameterTypes.map { it.canonicalName }
        return methodParamsTypes == paramTypes
    }
}
