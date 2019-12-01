package com.dpotenko.kirillweb.controller

import com.dpotenko.kirillweb.dto.CourseDto
import com.dpotenko.kirillweb.service.CourseService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
        val course = courseService.saveCourse(courseDto)
        return ResponseEntity.ok(course)
    }

    @GetMapping
    fun getAllCourses(): ResponseEntity<List<CourseDto>> {
        val courses = courseService.getAllCourses()
        return ResponseEntity.ok(courses)
    }

    @GetMapping("/{id}")
    fun getCourse(@PathVariable("id") id: Long): ResponseEntity<CourseDto> {
        val course = courseService.getCourseById(id)
        return ResponseEntity.ok(course)
    }
}