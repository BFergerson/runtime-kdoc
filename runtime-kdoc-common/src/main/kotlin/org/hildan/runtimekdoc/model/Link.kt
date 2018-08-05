package org.hildan.runtimekdoc.model

data class Link(
    val label: String,
    val referencedClassName: String?,
    val referencedMemberName: String?
)
