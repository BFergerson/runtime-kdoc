package org.hildan.runtimekdoc.model

/**
 * General Javadoc @tag, usually present after the main description. May be a standard tag like "@author" or a
 * user-defined custom tag.
 */
data class JavadocTag(
    /**
     * The name of the tag (including the <tt>@</tt>)
     */
    val name: String,
    /**
     * The description following the tag
     */
    val comment: Comment?
)

/**
 * Represents a <tt>@param</tt> tag on a class or method.
 */
data class ParamTag(val paramName: String, val comment: Comment?)

/**
 * Represents a <tt>@see</tt> tag on a class or method.
 */
sealed class SeeAlsoTag {
    data class SeeAlsoTextTag(val text: String): SeeAlsoTag()
    data class SeeAlsoUrlTag(val url: String, val label: String): SeeAlsoTag()
    data class SeeAlsoLinkTag(val link: Link): SeeAlsoTag()
}

/**
 * Represents a <tt>@throws</tt> tag on a class or method.
 */
data class ThrowsTag(val name: String, val comment: Comment?)
