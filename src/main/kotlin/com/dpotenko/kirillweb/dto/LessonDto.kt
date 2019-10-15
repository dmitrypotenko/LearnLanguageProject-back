package com.dpotenko.kirillweb.dto

data class LessonDto(
        val videoLink: String,
        val lessonText: String,
        val name: String,
        val attachments: List<AttachmentDto>,
        val order: Long,
        val id: Long
)