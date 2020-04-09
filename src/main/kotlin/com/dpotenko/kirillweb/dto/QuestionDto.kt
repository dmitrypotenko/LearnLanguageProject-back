package com.dpotenko.kirillweb.dto


data class QuestionDto(
        var question: String,
        var variants: MutableList<VariantDto>,
        val type: QuestionType,
        var id: Long?,
        var status: QuestionStatus = QuestionStatus.UNDEFINED)
