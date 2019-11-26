package com.dpotenko.kirillweb.controller

import com.dpotenko.kirillweb.dto.CourseDto
import com.dpotenko.kirillweb.service.CourseService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/courses")
class CourseController(val courseService: CourseService) {

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun submitCourse(@RequestBody courseDto: CourseDto): ResponseEntity<CourseDto> {
        println(courseDto)
        val course = courseService.saveCourse(courseDto)
        return ResponseEntity.ok(course)
    }


}