package org.hildan.runtimekdoc.reader

import org.hildan.runtimekdoc.model.Comment
import org.hildan.runtimekdoc.model.CommentElement
import org.hildan.runtimekdoc.model.CommentText
import org.hildan.runtimekdoc.model.InlineLink
import org.hildan.runtimekdoc.model.InlineTag
import org.hildan.runtimekdoc.model.Link

/**
 * Performs basic conversion of a Comment into a String. Subclasses are encouraged to override the [renderLink] method
 * to convert [Link]s to hyperlinks.
 */
open class CommentFormatter {

    /**
     * Renders the given [comment] as an HTML String.
     */
    fun format(comment: Comment?): String = comment?.joinToString(transform = ::renderElement) ?: ""

    protected open fun renderElement(e: CommentElement): String = when (e) {
        is CommentText -> renderText(e)
        is InlineLink -> renderLink(e.link)
        is InlineTag -> renderTag(e.name, e.value)
    }

    protected open fun renderText(text: CommentText) = text.value

    protected open fun renderLink(link: Link) = "{@link $link}"

    protected open fun renderTag(tagName: String, value: String) = when (tagName) {
        "code" -> renderCode(value)
        "literal" -> renderLiteral(value)
        else -> renderUnrecognizedTag(tagName, value)
    }

    protected open fun renderCode(text: String) = "<code>${escapeHtml(text)}</code>"

    protected open fun renderLiteral(text: String) = escapeHtml(text)

    protected open fun renderUnrecognizedTag(tagName: String, value: String) = "{@$tagName $value}"

    /**
     * Escapes the HTML special characters: `" & < >` in the given [value]
     */
    protected open fun escapeHtml(value: String): String = value.map { escapeHtmlChar(it) }.joinToString()

    private fun escapeHtmlChar(it: Char): Any = when (it) {
        '"' -> "&quot;"
        '&' -> "&amp;"
        '<' -> "&lt;"
        '>' -> "&gt;"
        else -> it
    }
}
