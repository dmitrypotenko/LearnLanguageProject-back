package com.dpotenko.kirillweb.controller

import com.dpotenko.kirillweb.dto.CourseDto
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/courses")
class CourseController {

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun submitCourse(@RequestBody courseDto: CourseDto): ResponseEntity<String> {
        println(courseDto)
        return ResponseEntity.ok("ok")
    }

    @GetMapping
    fun test(): ResponseEntity<String> {
        return ResponseEntity.ok("ok")
    }


}