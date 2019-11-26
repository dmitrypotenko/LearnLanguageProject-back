package com.dpotenko.kirillweb.dto

data class CourseDto(
        val name: String,
        val description: String?,
        val category: String?,
        val lessons: List<LessonDto>,
        val tests: List<TestDto>,
        var id: Long?
)