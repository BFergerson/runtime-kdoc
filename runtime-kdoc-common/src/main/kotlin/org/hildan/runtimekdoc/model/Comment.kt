package org.hildan.runtimekdoc.model

/**
 * Comment text that may contain inline tags.
 */
data class Comment(val elements: List<CommentElement>) : Iterable<CommentElement> {

    override fun iterator(): Iterator<CommentElement> = elements.iterator()
}

sealed class CommentElement

data class CommentText(val value: String) : CommentElement()
data class InlineLink(val link: Link) : CommentElement()
data class InlineTag(val name: String, val value: String) : CommentElement()
