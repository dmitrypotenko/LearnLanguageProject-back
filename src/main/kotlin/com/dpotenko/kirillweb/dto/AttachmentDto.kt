package com.dpotenko.kirillweb.dto

data class AttachmentDto(
        val attachmentLink: String,
        val attachmentTitle: String,
        var id: Long?
)