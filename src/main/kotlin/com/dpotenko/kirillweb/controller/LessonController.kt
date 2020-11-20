package com.dpotenko.kirillweb.controller

import com.dpotenko.kirillweb.domain.UserPrincipal
import com.dpotenko.kirillweb.dto.LessonDto
import com.dpotenko.kirillweb.service.AttachmentService
import com.dpotenko.kirillweb.service.LessonService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/lessons")
class LessonController(val lessonService: LessonService, attachmentService: AttachmentService) {

    @PostMapping("/completed/{lessonId}")
    fun setAsCompleted(@PathVariable lessonId: Long, @AuthenticationPrincipal userPrincipal: UserPrincipal?): ResponseEntity<Void> {
        userPrincipal?.let { lessonService.markAsCompleted(lessonId, it.id) }
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{lessonId}")
    fun getFullLesson(@PathVariable lessonId: Long): LessonDto {
        return lessonService.getLessonsById(lessonId)
    }
}
