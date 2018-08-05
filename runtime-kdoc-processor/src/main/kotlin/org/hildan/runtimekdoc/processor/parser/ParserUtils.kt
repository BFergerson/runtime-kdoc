package org.hildan.runtimekdoc.processor.parser

import org.hildan.runtimekdoc.model.Link

private val whitespace = Regex("""\s""")
private val linkRefSplitter = Regex("#")
private val htmlAnchor = Regex("""<a\s*href="([^"])"\s*>(.*)</\s*a>""")

internal data class FirstWordAccessor(val firstWord: String, val rest: String) {
    val restAsComment
        get() = CommentParser.parse(rest)
}

internal fun splitFirstWord(text: String): FirstWordAccessor {
    val s = whitespace.split(text.trim(), 2)
    val name = s[0]
    val value = if (s.size > 1) s[1] else ""
    return FirstWordAccessor(name, value)
}

internal fun parseLink(value: String): Link {
    val linkElts = whitespace.split(value, 2)
    val label = if (linkElts.size > 1) linkElts[1] else linkElts[0]

    val ref = linkRefSplitter.split(linkElts[0], 2)
    val classRef = ref[0]
    val memberRef = if (ref.size > 1) ref[1] else null

    return Link(label, classRef, memberRef)
}

internal data class AnchorTag(val url: String, val text: String)

internal fun parseHtmlAnchor(text: String): AnchorTag {
    val match = htmlAnchor.matchEntire(text.trim()) ?: throw IllegalArgumentException("malformed HTML anchor: $text")
    return AnchorTag(match.groupValues[1], match.groupValues[2])
}
