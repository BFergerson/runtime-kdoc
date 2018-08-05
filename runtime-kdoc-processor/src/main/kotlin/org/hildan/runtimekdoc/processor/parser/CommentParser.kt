package org.hildan.runtimekdoc.processor.parser

import org.hildan.runtimekdoc.model.Comment
import org.hildan.runtimekdoc.model.CommentElement
import org.hildan.runtimekdoc.model.CommentText
import org.hildan.runtimekdoc.model.InlineLink
import org.hildan.runtimekdoc.model.InlineTag

internal object CommentParser {

    private val inlineTag = Regex("""\{@(\w+)(?:\s+(\w[^}]+)?)?}""")

    fun parse(commentText: String): Comment? = when {
        commentText.isBlank() -> null
        else -> Comment(parseElements(commentText.trim()))
    }

    private fun parseElements(commentText: String): List<CommentElement> {
        val matches = inlineTag.findAll(commentText)
        val elements = mutableListOf<CommentElement>()
        var pos = 0
        for (match in matches) {
            val start = match.range.start
            if (start > pos) {
                elements.add(CommentText(commentText.substring(pos, start)))
            }
            val elt = createTagElement(match.groupValues[1], match.groupValues[2])
            elements.add(elt)
            pos = match.range.endInclusive + 1
        }

        if (pos < commentText.length) {
            elements.add(CommentText(commentText.substring(pos)))
        }
        return elements
    }

    private fun createTagElement(name: String, value: String): CommentElement = when (name) {
        "link" -> InlineLink(parseLink(value))
        else -> InlineTag(name, value)
    }
}
