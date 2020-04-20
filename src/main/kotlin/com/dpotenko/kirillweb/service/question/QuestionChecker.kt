package com.dpotenko.kirillweb.service.question

import com.dpotenko.kirillweb.dto.QuestionDto
import com.dpotenko.kirillweb.dto.QuestionType
import com.dpotenko.kirillweb.dto.VariantDto
import org.springframework.stereotype.Component


interface QuestionChecker {
    fun types(): List<QuestionType>

    fun checkQuestion(userQuestion: QuestionDto,
                      realQuestion: QuestionDto): Boolean
}

@Component
class SimpleQuestionChecker : QuestionChecker {
    override fun types(): List<QuestionType> {
        return listOf(QuestionType.SINGLE_CHOICE, QuestionType.MULTIPLE_CHOICE)
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
        return realQuestion.variants.find { it.id == userVariant.id }
    }
}

@Component
class SelectWordsChecker : QuestionChecker {
    override fun types(): List<QuestionType> {
        return listOf(QuestionType.CUSTOM_INPUT)
    }

    override fun checkQuestion(userQuestion: QuestionDto,
                               realQuestion: QuestionDto): Boolean {
        var result = true
        if (userQuestion.variants.size != realQuestion.variants.size) {
            result = false
        }
        val toAdd = mutableListOf<VariantDto>()
        userQuestion.variants.forEach { userVariant ->

            val foundActualVariant = findActualOption(realQuestion, userVariant)
            foundActualVariant?.let { actualVariant ->
                userVariant.explanation = actualVariant.explanation

                if (userVariant.inputType == "input") {
                    val isEquals = checkInputIsRight(userVariant, actualVariant)
                    if (!isEquals) {
                        toAdd.add(foundActualVariant)
                        result = false
                    }
                } else {
                    userVariant.id = actualVariant.id
                    val isSelectionRight = checkSelectIsRight(userVariant, actualVariant)
                    if (!isSelectionRight) {
                        result = false;
                    }
                }
            } ?: also {
                result = false
                userVariant.isWrong = true
            }
        }

        userQuestion.variants.addAll(toAdd)

        return result
    }

    private fun checkInputIsRight(userVariant: VariantDto,
                                  actualVariant: VariantDto): Boolean {
        if (!userVariant.isTicked) {
            return false
        }
        val transformedOption = userVariant.variant.replace("//s+".toRegex(), " ").trim()
        val isEquals = transformedOption.equals(actualVariant.variant, true)
        if (isEquals) {
            userVariant.isRight = true
        }
        return isEquals
    }

    private fun checkSelectIsRight(userVariant: VariantDto,
                                   actualVariant: VariantDto): Boolean {
        if (userVariant.isTicked && actualVariant.isRight) {
            userVariant.isRight = true
        } else if (actualVariant.isRight) {
            userVariant.isRight = true
            return false
        } else if (userVariant.isTicked) {
            userVariant.isWrong = true
            return false
        }

        return true
    }

    private fun findActualOption(realQuestion: QuestionDto,
                                 userVariant: VariantDto): VariantDto? {
        val foundVariant = realQuestion.variants.find { checkOptionsMatching(it, userVariant) }
        return foundVariant
    }

    private fun checkOptionsMatching(it: VariantDto,
                                     userVariant: VariantDto): Boolean {
        if (userVariant.inputType == "input") {
            return it.inputName == userVariant.inputName && !it.isTicked
        }
        return it.variant == userVariant.variant && it.inputName == userVariant.inputName
    }


}
