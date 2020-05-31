package com.dpotenko.kirillweb.controller.validate

import com.dpotenko.kirillweb.dto.QuestionType
import com.dpotenko.kirillweb.dto.TestDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class TestDataValidator {
    fun validate(test: TestDto) {
        test.questions.forEach { question ->
            if (QuestionType.CUSTOM_INPUT == question.type || QuestionType.SINGLE_CHOICE == question.type )
            question.variants.groupBy { option -> option.inputName }.forEach { entry ->
                if (entry.value.filter { it.isTicked }.size > 1) {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Found several answers for one input. Data is invalid!")
                }
            }

        }
    }
}
