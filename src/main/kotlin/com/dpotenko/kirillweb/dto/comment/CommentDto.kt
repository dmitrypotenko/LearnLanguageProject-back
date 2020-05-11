package com.dpotenko.kirillweb.dto.comment

import java.time.LocalDateTime
import javax.validation.constraints.NotBlank

data class CommentDto(
        var id: Long?,
        @NotBlank val commentText: String,
        @NotBlank val threadId: String,
        var createDate: LocalDateTime?,
        var updateDate: LocalDateTime?,
        var editable: Boolean,
        var userName: String?,
        var userPic: String?
)
