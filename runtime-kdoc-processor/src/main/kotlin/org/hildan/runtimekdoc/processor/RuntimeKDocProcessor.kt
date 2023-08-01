package org.hildan.runtimekdoc.processor

import io.vertx.core.json.Json
import org.hildan.runtimekdoc.DOC_RESOURCE_SUFFIX
import org.hildan.runtimekdoc.annotations.RetainDoc
import org.hildan.runtimekdoc.model.ClassDoc
import org.hildan.runtimekdoc.processor.parser.DocParser
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import javax.tools.FileObject
import javax.tools.StandardLocation
import kotlin.text.Charsets.UTF_8

class RuntimeKDocProcessor : AbstractProcessor() {

    private lateinit var docParser: DocParser

    override fun process(annotations: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
        this.docParser = DocParser(processingEnv)

        // Make sure each element only gets processed once.
        val alreadyProcessed = mutableSetOf<Element>()
        elementsToProcess(annotations, roundEnvironment).forEach { generateJavadoc(it, alreadyProcessed) }
        return false
    }

    private fun elementsToProcess(annotations: Set<TypeElement>, roundEnvironment: RoundEnvironment): Set<Element> {
        // if we retain Javadoc for all packages, there is no need to look for @RetainDoc annotation
        val packagesOption = processingEnv.options["javadoc.packages"] ?: return roundEnvironment.rootElements

        val packageFilter = PackageFilter(packagesOption)::test
        val packageFilteredElements = roundEnvironment.rootElements.filter(packageFilter).toSet()
        return packageFilteredElements + retainDocAnnotatedElements(annotations, roundEnvironment)
    }

    private fun retainDocAnnotatedElements(
        annotations: Set<TypeElement>,
        roundEnvironment: RoundEnvironment
    ): Set<Element> {
        for (ann in annotations) {
            if (isRetainDocAnnotation(ann)) {
                return roundEnvironment.getElementsAnnotatedWith(ann)
            }
        }
        return emptySet()
    }

    private fun isRetainDocAnnotation(annotation: TypeElement): Boolean =
        annotation.qualifiedName.toString() == RetainDoc::class.java.name || annotation.getAnnotation(RetainDoc::class.java) != null

    private fun generateJavadoc(element: Element, alreadyProcessed: MutableSet<Element>) {
        val kind = element.kind
        if (kind == ElementKind.CLASS || kind == ElementKind.INTERFACE || kind == ElementKind.ENUM) {
            try {
                generateJavadocForClass(element, alreadyProcessed)
            } catch (ex: Exception) {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Javadoc retention failed; $ex", element)
                throw RuntimeException("Javadoc retention failed for $element", ex)
            }
        }

        element.enclosedElements.forEach { generateJavadoc(it, alreadyProcessed) }
    }

    private fun generateJavadocForClass(element: Element, alreadyProcessed: MutableSet<Element>) {
        if (!alreadyProcessed.add(element)) {
            return
        }
        val classElement = element as TypeElement
        val classDoc = docParser.parseClassDoc(classElement)
        classDoc?.also { outputJsonDoc(classElement, toJson(it)) }
    }

    private fun toJson(doc: ClassDoc): String {
        return Json.encode(doc)
    }

    private fun outputJsonDoc(classElement: TypeElement, classJsonDoc: String) {
        val resource = createJavadocResourceFile(classElement)
        resource.openOutputStream().use { os -> os.write(classJsonDoc.toByteArray(UTF_8)) }
    }

    private fun createJavadocResourceFile(classElement: TypeElement): FileObject {
        val packageElement = getPackageElement(classElement)
        val packageName = packageElement.qualifiedName.toString()
        val relativeName = getClassName(classElement) + DOC_RESOURCE_SUFFIX
        return processingEnv.filer
            .createResource(StandardLocation.CLASS_OUTPUT, packageName, relativeName, classElement)
    }

    private fun getPackageElement(element: Element): PackageElement =
        element as? PackageElement ?: getPackageElement(element.enclosingElement)

    private fun getClassName(typeElement: TypeElement): String {
        // we can't take the simple name if we want to return names like EnclosingClass$NestedClass
        var typeName = typeElement.qualifiedName.toString()
        val packageName = getPackageElement(typeElement).qualifiedName.toString()

        if (!packageName.isEmpty()) {
            typeName = typeName.substring(packageName.length + 1)
            typeName = typeName.replace(".", "$")
        }
        return typeName
    }

    override fun getSupportedAnnotationTypes(): Set<String> = setOf(RetainDoc::class.java.name)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()
}
