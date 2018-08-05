package org.hildan.runtimekdoc.model

data class JavadocComment(
    val description: Comment?,
    val params: List<ParamTag>,
    val returns: Comment?,
    val throws: List<ThrowsTag>,
    val seeAlso: List<SeeAlsoTag>,
    val other: List<JavadocTag>
)
