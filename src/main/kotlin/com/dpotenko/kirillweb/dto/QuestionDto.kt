package com.dpotenko.kirillweb.dto


data class QuestionDto(
        val question: String,
        var variants: List<VariantDto>,
        val type: QuestionType,
        var id: Long?,
        var status: QuestionStatus = QuestionStatus.UNDEFINED)