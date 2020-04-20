package com.dpotenko.kirillweb.service.question

import com.dpotenko.kirillweb.dto.VariantDto
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component

interface OptionParser {
    fun parseOptions(element: Element): List<VariantDto>

    val tagName: String
}

@Component
class SelectOptionParser : OptionParser {
    override fun parseOptions(select: Element): List<VariantDto> {
        return select.select("option")
                .map { option ->
                    val selectName = select.attr("name")
                    VariantDto(option.text(),
                            isRight = option.attr("selected").isNotBlank(),
                            isWrong = false,
                            isTicked = false,
                            explanation = "",
                            id = null,
                            inputName = selectName,
                            inputType = "select")
                }
    }

    override val tagName: String
        get() = "select"
}

@Component
class InputOptionParser : OptionParser {
    override fun parseOptions(input: Element): List<VariantDto> {
        val inputName = input.attr("name")
        val option = input.attr("value")
        return listOf(VariantDto(
                option,
                isRight = true,
                isWrong = false,
                isTicked = false,
                explanation = "",
                id = null,
                inputName = inputName,
                inputType = "input"
        ))
    }

    override val tagName: String
        get() = "input"
}
