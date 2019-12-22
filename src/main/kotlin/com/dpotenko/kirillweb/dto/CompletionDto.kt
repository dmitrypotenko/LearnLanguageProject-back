package com.dpotenko.kirillweb.dto

data class CompletionDto(
        val isStarted: Boolean,
        val isCompleted: Boolean,
        val successRate: Double,
        val completionPercent: Double
)
