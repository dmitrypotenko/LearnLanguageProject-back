package com.dpotenko.kirillweb.service.question

import com.dpotenko.kirillweb.dto.QuestionDto
import com.dpotenko.kirillweb.dto.QuestionType
import com.dpotenko.kirillweb.dto.VariantDto
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component


interface QuestionChecker {
    fun types(): List<QuestionType>

    fun checkQuestion(userQuestion: QuestionDto,
                      realQuestion: QuestionDto): Boolean
}

@Component
class SimpleQuestionChecker : QuestionChecker {
    override fun types(): List<QuestionType> {
        return listOf(QuestionType.SINGLE_CHOICE, QuestionType.MULTIPLE_CHOICE, QuestionType.SELECT_WORDS)
    }

    override fun checkQuestion(userQuestion: QuestionDto,
                               realQuestion: QuestionDto): Boolean {
        var result = true
        if (userQuestion.variants.size != realQuestion.variants.size) {
            result = false
        }
        userQuestion.variants.forEach { userVariant ->
            val foundActualVariant = findActualOption(realQuestion, userVariant)
            foundActualVariant?.let { actualVariant ->
                userVariant.explanation = actualVariant.explanation

                if (userVariant.isTicked && actualVariant.isRight) {
                    userVariant.isRight = true
                } else if (actualVariant.isRight) {
                    result = false
                    userVariant.isRight = true
                } else if (userVariant.isTicked) {
                    result = false
                    userVariant.isWrong = true
                }
            }
        }

        return result
    }

    private fun findActualOption(realQuestion: QuestionDto,
                                 userVariant: VariantDto): VariantDto? {
        if (realQuestion.type == QuestionType.SELECT_WORDS) {
            val foundVariant = realQuestion.variants.find { it.inputName == userVariant.inputName && it.variant == userVariant.variant}
            userVariant.id = foundVariant?.id
            return foundVariant
        }
       return realQuestion.variants.find { it.id == userVariant.id }
    }
}

@Component
class SelectWordsChecker : QuestionChecker {
    override fun types(): List<QuestionType> {
        return listOf()
    }

    override fun checkQuestion(userQuestion: QuestionDto,
                               realQuestion: QuestionDto): Boolean {
        userQuestion.variants = mutableListOf()
        val realSelects = Jsoup.parse(realQuestion.question).select("select")

        var isCorrect = true

        Jsoup.parse(userQuestion.question).select("select")
                .forEach({ select ->
                    val userOption = findCorrectOption(select)
                    val selectName = select.attr("name")
                    val matchedRealSelect = realSelects.find { realSelect -> realSelect.attr("name") == selectName }
                            ?: throw SelectNotFoundException("Can't find select for $selectName")
                    val realCorrectOption = findCorrectOption(matchedRealSelect)
                    if (realCorrectOption == null || userOption == null || realCorrectOption != userOption) {
                        userQuestion.variants.add(VariantDto(realCorrectOption?: "No option"
                                ?: "", isRight = true, isWrong = false, isTicked = false, explanation = "", id = 0L, inputName = null))
                        isCorrect = false
                    }
                })

        return isCorrect
    }

    private fun findCorrectOption(select: Element): String? {
        return select.select("option")
                .find { option -> option.attr("selected").isNotBlank() }?.text()
    }

}
