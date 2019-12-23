package com.dpotenko.kirillweb.dto

data class TestDto(
        var questions: List<QuestionDto>,
        val name: String,
        val order: Long,
        var id: Long?
) {
    var completion: CompletionDto? = null
}