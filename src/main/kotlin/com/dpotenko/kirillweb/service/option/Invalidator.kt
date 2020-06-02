package com.dpotenko.kirillweb.service.option

import com.dpotenko.kirillweb.dto.VariantDto
import org.springframework.stereotype.Service


interface Invalidator {
    fun invalidate(option: VariantDto): VariantDto
}

@Service
class GeneralInvalidator : Invalidator {
    override fun invalidate(option: VariantDto): VariantDto {
        option.isRight = false
        option.explanation = ""
        if (option.inputType == "input") {
            option.isTicked = true
            option.variant = ""
        }
        return option;
    }
}

