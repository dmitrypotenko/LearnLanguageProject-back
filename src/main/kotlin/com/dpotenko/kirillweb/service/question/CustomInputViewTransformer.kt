package com.dpotenko.kirillweb.service.question

import org.jsoup.nodes.Attributes
import org.jsoup.nodes.Element
import org.jsoup.nodes.FormElement
import org.jsoup.parser.Tag
import org.springframework.stereotype.Component

interface CustomInputViewTransformer {
    fun transform(customInput: Element): Element

    val tagName: String
}

@Component
class SelectViewTransformer : CustomInputViewTransformer {
    override fun transform(customInput: Element): Element {
        val attributes = Attributes()
        attributes.add("name", customInput.attr("name"))
        customInput.replaceWith(FormElement(Tag.valueOf("select-element"), null, attributes))
        return customInput
    }

    override val tagName: String
        get() = "select"
}

@Component
class InputViewTransformer : CustomInputViewTransformer {
    override fun transform(customInput: Element): Element {
        val attributes = Attributes()
        attributes.add("name", customInput.attr("name"))
        customInput.replaceWith(FormElement(Tag.valueOf("input-element"), null, attributes))
        return customInput
    }

    override val tagName: String
        get() = "input"
}
