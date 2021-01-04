package com.dpotenko.lessonsbox.dto

data class LessonDto(
        val videoLink: String?,
        val lessonText: String,
        val name: String?,
        var attachments: List<AttachmentDto>,
        val order: Long,
        var id: Long?
) {
    var isCompleted: Boolean = false
}
