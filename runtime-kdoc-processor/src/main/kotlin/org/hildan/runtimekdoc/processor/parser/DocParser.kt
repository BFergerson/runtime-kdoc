package org.hildan.runtimekdoc.processor.parser

import org.hildan.runtimekdoc.model.ClassDoc
import org.hildan.runtimekdoc.model.FieldDoc
import org.hildan.runtimekdoc.model.MethodDoc
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

internal class DocParser(private val processingEnv: ProcessingEnvironment) {

    fun parseClassDoc(type: TypeElement): ClassDoc? {
        val name = type.simpleName.toString()
        val javadoc = processingEnv.elementUtils.getDocComment(type)?.trim() ?: ""

        val children = type.enclosedElements.groupBy { it.kind }

        val enclosedFields = children[ElementKind.FIELD] ?: emptyList()
        val enclosedEnumConstants = children[ElementKind.ENUM_CONSTANT] ?: emptyList()
        val enclosedMethods = children[ElementKind.METHOD] ?: emptyList()

        val fieldDocs = enclosedFields.mapNotNull(this::parseFieldDoc)
        val enumConstantDocs = enclosedEnumConstants.mapNotNull(this::parseFieldDoc)
        val methodDocs = enclosedMethods.mapNotNull(this::parseMethodDoc)

        if (javadoc.isBlank() && fieldDocs.isEmpty() && enumConstantDocs.isEmpty() && methodDocs.isEmpty()) {
            return null
        }
        return ClassDoc(
            name,
            fieldDocs,
            enumConstantDocs,
            methodDocs,
            JavadocParser.parseJavadoc(javadoc)
        )
    }

    private fun parseFieldDoc(field: Element): FieldDoc? {
        val javadoc = processingEnv.elementUtils.getDocComment(field) ?: return null
        val name = field.simpleName.toString()
        return FieldDoc(name, JavadocParser.parseJavadoc(javadoc))
    }

    private fun parseMethodDoc(method: Element): MethodDoc? {
        assert(method is ExecutableElement)

        val javadoc = processingEnv.elementUtils.getDocComment(method) ?: return null
        val name = method.simpleName.toString()
        val paramTypes = getParamErasures(method as ExecutableElement)
        return MethodDoc(name, paramTypes, JavadocParser.parseJavadoc(javadoc))
    }

    private fun getParamErasures(executableElement: ExecutableElement): List<String> = executableElement.parameters
        .map { it.asType() }
        .map { processingEnv.typeUtils.erasure(it) }
        .map { it.toString() }
}
