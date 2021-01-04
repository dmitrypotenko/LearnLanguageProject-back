package com.dpotenko.lessonsbox.dto

@JooqEntity
data class SearchResultDto(var orderNumber: Long,
                           var courseId: Long,
                           var matchedExtract: String,
                           var lessonName: String,
                           var courseName: String)
