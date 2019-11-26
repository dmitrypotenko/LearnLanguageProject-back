package com.dpotenko.kirillweb.dto

data class TestDto(
        val questions: List<QuestionDto>,
        val name: String,
        val order: Long,
        var id: Long?
)