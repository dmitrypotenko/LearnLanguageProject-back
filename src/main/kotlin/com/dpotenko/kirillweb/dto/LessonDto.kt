package com.dpotenko.kirillweb.dto

data class LessonDto(
        val videoLink: String?,
        val lessonText: String,
        val name: String?,
        var attachments: List<AttachmentDto>,
        val order: Long,
        var id: Long?
)