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
        val toAdd = mutableListOf<VariantDto>()
        realQuestion.variants.filter { variant -> variant.isRight }.forEach { rightOption ->

            val foundUserOption = findUserOption(userQuestion, rightOption)
            foundUserOption?.let { userOption ->
                userOption.explanation = rightOption.explanation

                if (userOption.inputType == "input") {
                    val isRight = checkInputIsRight(userOption, rightOption)
                    if (isRight) {
                        userOption.isRight = true
                    }
                    toAdd.add(rightOption)
                } else {
                    userOption.isRight = true
                    result = userOption.isTicked
                }
            } ?: also {
                result = false
            }
        }

        if (userQuestion.variants.find { variantDto -> variantDto.isTicked && !variantDto.isRight } != null) {
            result = false
        }

        if (!result) {
            userQuestion.variants.addAll(toAdd)
        }

        return result
    }

    private fun checkInputIsRight(userVariant: VariantDto,
                                  actualVariant: VariantDto): Boolean {
        if (!userVariant.isTicked) {
            return false
        }
        val transformedOption = userVariant.variant.replace("\\s+".toRegex(), " ").trim()
        val isEquals = transformedOption.equals(actualVariant.variant, true)
        if (isEquals) {
            userVariant.isRight = true
        }
        return isEquals
    }

    private fun findUserOption(userQuestion: QuestionDto,
                               rightOption: VariantDto): VariantDto? {
        val foundVariants = userQuestion.variants.filter { userVariant -> checkOptionsMatching(userVariant, rightOption) }
        return foundVariants.getOrNull(0)
    }

    private fun checkOptionsMatching(userVariant: VariantDto,
                                     rightVariant: VariantDto): Boolean {
        if (rightVariant.inputType == "input") {
            return userVariant.inputName == rightVariant.inputName && userVariant.isTicked
        }
        return userVariant.variant == rightVariant.variant && userVariant.inputName == rightVariant.inputName
    }


}
