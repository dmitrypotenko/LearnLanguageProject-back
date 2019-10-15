package com.dpotenko.kirillweb.dto

data class QuestionDto(val questionText: String,
                       val variants: List<VariantDto>,
                       val type: QuestionType,
                       val id: Long)