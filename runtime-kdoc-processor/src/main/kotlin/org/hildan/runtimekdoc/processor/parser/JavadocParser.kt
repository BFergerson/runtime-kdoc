package org.hildan.runtimekdoc.processor.parser

import org.hildan.runtimekdoc.model.Comment
import org.hildan.runtimekdoc.model.JavadocComment
import org.hildan.runtimekdoc.model.JavadocTag
import org.hildan.runtimekdoc.model.ParamTag
import org.hildan.runtimekdoc.model.SeeAlsoTag
import org.hildan.runtimekdoc.model.SeeAlsoTag.SeeAlsoLinkTag
import org.hildan.runtimekdoc.model.SeeAlsoTag.SeeAlsoTextTag
import org.hildan.runtimekdoc.model.SeeAlsoTag.SeeAlsoUrlTag
import org.hildan.runtimekdoc.model.ThrowsTag

internal object JavadocParser {

    private val blockSeparator = Regex("""^\s*@(?=\S)""", RegexOption.MULTILINE)

    private data class TagBlock(val name: String, val text: String)

    private data class ParsedJavadoc(val description: Comment?, val tagBlocks: List<TagBlock>)

    fun parseJavadoc(javadoc: String): JavadocComment {
        val (description, tags) = parse(javadoc.trim())

        val paramTags = mutableListOf<ParamTag>()
        val throwTags = mutableListOf<ThrowsTag>()
        val otherTags = mutableListOf<JavadocTag>()
        val seeAlsoTags = mutableListOf<SeeAlsoTag>()
        var returnDesc: Comment? = null

        for (tag in tags) {
            when (tag.name) {
                "return" -> returnDesc = CommentParser.parse(tag.text)
                "param" -> paramTags.add(parseParamTag(tag))
                "throws", "exception" -> throwTags.add(parseThrowsTag(tag))
                "see" -> seeAlsoTags.add(parseSeeAlsoTag(tag))
                else -> otherTags.add(parseTag(tag))
            }
        }

        return JavadocComment(
            description = description,
            params = paramTags,
            returns = returnDesc,
            throws = throwTags,
            seeAlso = seeAlsoTags,
            other = otherTags
        )
    }

    private fun parse(javadoc: String): ParsedJavadoc {
        val blocks = blockSeparator.split(javadoc)
        val description = blocks[0].trim()
        val blockTags = blocks.drop(1).map(::splitFirstWord).map { TagBlock(it.firstWord, it.rest) }

        return ParsedJavadoc(CommentParser.parse(description), blockTags)
    }

    private fun parseTag(tagBlock: TagBlock) =
        JavadocTag(tagBlock.name, CommentParser.parse(tagBlock.text))

    private fun parseParamTag(tagBlock: TagBlock): ParamTag {
        val tagText = splitFirstWord(tagBlock.text)
        return ParamTag(tagText.firstWord, tagText.restAsComment)
    }

    private fun parseThrowsTag(tagBlock: TagBlock): ThrowsTag {
        val tagText = splitFirstWord(tagBlock.text)
        return ThrowsTag(tagText.firstWord, tagText.restAsComment)
    }

    private fun parseSeeAlsoTag(tagBlock: TagBlock): SeeAlsoTag {
        if (tagBlock.text.isBlank()) {
            return SeeAlsoTextTag("")
        }
        // distinction by the first character reflects the actual implementation of the Javadoc tool
        return when (tagBlock.text[0]) {
            '"' -> SeeAlsoTextTag(tagBlock.text)
            '<' -> parseHtmlAnchor(tagBlock.text).run { SeeAlsoUrlTag(url, text) }
            else -> SeeAlsoLinkTag(parseLink(tagBlock.text))
        }
    }
}
