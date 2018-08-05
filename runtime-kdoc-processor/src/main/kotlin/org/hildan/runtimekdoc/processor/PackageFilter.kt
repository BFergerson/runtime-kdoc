package org.hildan.runtimekdoc.processor

import java.util.function.Predicate
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.QualifiedNameable

internal class PackageFilter(commaDelimitedPackages: String) : Predicate<Element> {

    private val rootPackages: Set<String> = commaDelimitedPackages.split(",")
        .filter(String::isNotBlank)
        .map(String::trim)
        .toSet()

    private val cache: MutableMap<String, Boolean> = mutableMapOf()

    override fun test(element: Element): Boolean {
        val elementPackage = getPackage(element)

        val cachedValue = cache[elementPackage]
        if (cachedValue != null) {
            return cachedValue
        }

        val whitelisted = rootPackages.any { elementPackage.startsWith("$it.") }
        cache[elementPackage] = whitelisted
        return whitelisted
    }

    private fun getPackage(element: Element): String {
        var current = element
        while (current.kind != ElementKind.PACKAGE) {
            current = current.enclosingElement
            if (current == null) {
                return ""
            }
        }
        return (current as QualifiedNameable).qualifiedName.toString()
    }
}
