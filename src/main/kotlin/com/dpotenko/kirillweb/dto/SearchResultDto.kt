package com.dpotenko.kirillweb.dto

@JooqEntity
data class SearchResultDto(var orderNumber: Long,
                           var courseId: Long,
                           var matchedExtract: String,
                           var lessonName: String)
