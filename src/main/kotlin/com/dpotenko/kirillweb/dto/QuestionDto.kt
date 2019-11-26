package com.dpotenko.kirillweb.dto


data class QuestionDto(
        val question: String,
        val variants: List<VariantDto>,
        val type: QuestionType,
        var id: Long?)