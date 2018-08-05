package org.hildan.runtimekdoc.processor.parser

import io.kotlintest.data.forall
import io.kotlintest.inspectors.forAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.ShouldSpec
import io.kotlintest.tables.row
import org.hildan.runtimekdoc.model.Comment
import org.hildan.runtimekdoc.model.CommentText
import org.hildan.runtimekdoc.model.InlineLink
import org.hildan.runtimekdoc.model.InlineTag
import org.hildan.runtimekdoc.model.Link

internal class CommentParserTest : ShouldSpec({

    should("yield null on blank input") {
        listOf("", " ", "   ", "\n", "\t\n", "  \n  ").forAll {
            CommentParser.parse(it) shouldBe null
        }
    }

    should("yield a single text node on normal text") {
        listOf("abcdef", "abc def\nxyz", "abc}def", "{abc}def").forAll {
            CommentParser.parse(it) shouldBe Comment(listOf(CommentText(it)))
        }
    }

    should("yield a comment with a single InlineLink element") {
        forall(
            row("{@link ClassName}", "ClassName", "ClassName", null),
            row("{@link ClassName myLabel}", "myLabel", "ClassName", null),
            row("{@link ClassName my label}", "my label", "ClassName", null),
            row("{@link ClassName#member}", "ClassName#member", "ClassName", "member"),
            row("{@link ClassName#member myLabel}", "myLabel", "ClassName", "member")
        ) { input, label, refClassName, refMemberName ->
            CommentParser.parse(input) shouldBe Comment(listOf(InlineLink(Link(label, refClassName, refMemberName))))
        }
    }

    should("yield a comment with a single InlineTag element") {
        forall(
            row("{@sometag someValue}", "sometag", "someValue"),
            row("{@sometag some value}", "sometag", "some value"),
            row("{@sometag}", "sometag", ""),
            row("{@sometag }", "sometag", ""),
            row("{@sometag   \t  }", "sometag", "")
        ) { input, tagName, text ->
            CommentParser.parse(input) shouldBe Comment(listOf(InlineTag(tagName, text)))
        }
    }

    should("handle mixed text and arbitrary tag") {
        CommentParser.parse("text before {@sometag some value}") shouldBe Comment(
            listOf(
                CommentText("text before "),
                InlineTag("sometag", "some value")
            )
        )
    }

    should("handle mixed text and link") {
        CommentParser.parse("text before {@link ClassName} text after") shouldBe Comment(
            listOf(
                CommentText("text before "),
                InlineLink(Link("ClassName", "ClassName", null)),
                CommentText(" text after")
            )
        )
        CommentParser.parse("Adds the given {@link Action}s to the queue.") shouldBe Comment(
            listOf(
                CommentText("Adds the given "),
                InlineLink(Link("Action", "Action", null)),
                CommentText("s to the queue.")
            )
        )
        CommentParser.parse("Adds the given {@link Action}s to the queue.") shouldBe Comment(
            listOf(
                CommentText("Adds the given "),
                InlineLink(Link("Action", "Action", null)),
                CommentText("s to the queue.")
            )
        )
    }

    should("handle weird braces") {
        CommentParser.parse("text}before {@link ClassName} text{after") shouldBe Comment(
            listOf(
                CommentText("text}before "),
                InlineLink(Link("ClassName", "ClassName", null)),
                CommentText(" text{after")
            )
        )
        CommentParser.parse("  text}bef{}ore {@link ClassName}{@} text{after}\nand {@empty}\n\n") shouldBe Comment(
            listOf(
                CommentText("text}bef{}ore "),
                InlineLink(Link("ClassName", "ClassName", null)),
                CommentText("{@} text{after}\nand "),
                InlineTag("empty", "")
            )
        )
    }
})
