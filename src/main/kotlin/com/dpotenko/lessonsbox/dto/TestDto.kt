package com.dpotenko.lessonsbox.dto

data class TestDto(
        var questions: List<QuestionDto>,
        val name: String,
        val instruction: String?,
        val order: Long,
        var id: Long?,
        val successThreshold: Int,
        val isRetryable: Boolean
) {
    var isCompleted: Boolean = false
}
