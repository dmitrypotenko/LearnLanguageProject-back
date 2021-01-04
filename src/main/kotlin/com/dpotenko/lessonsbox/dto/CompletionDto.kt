package com.dpotenko.lessonsbox.dto

data class CompletionDto(
        val isStarted: Boolean,
        val isCompleted: Boolean,
        val successRate: Double,
        val completionPercent: Double
)
