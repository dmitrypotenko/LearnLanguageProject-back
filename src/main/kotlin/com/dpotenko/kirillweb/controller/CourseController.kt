package com.dpotenko.kirillweb.controller

import com.dpotenko.kirillweb.domain.UserPrincipal
import com.dpotenko.kirillweb.dto.CourseDto
import com.dpotenko.kirillweb.service.CourseService
import com.dpotenko.kirillweb.service.OwnerService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/courses")
class CourseController(val courseService: CourseService,
                       val ownerService: OwnerService) {

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun submitCourse(@RequestBody courseDto: CourseDto, @AuthenticationPrincipal userPrincipal: UserPrincipal?): ResponseEntity<CourseDto> {
        ownerService.checkIsAllowedToEdit(courseDto.id, userPrincipal)
        val course = courseService.saveCourse(courseDto)
        ownerService.saveCreator(userPrincipal, courseDto)
        return ResponseEntity.ok(course)
    }

    @GetMapping
    fun getAllCourses(@AuthenticationPrincipal userPrincipal: UserPrincipal?): ResponseEntity<List<CourseDto>> {
        val courses = courseService.getAllCourses(userPrincipal)
        return ResponseEntity.ok(courses)
    }

    @GetMapping("/{id}")
    fun getCourse(@PathVariable("id") id: Long, @AuthenticationPrincipal userPrincipal: UserPrincipal?): ResponseEntity<CourseDto> {
        val course = courseService.getCourseById(id, userPrincipal)
        return ResponseEntity.ok(course)
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun getCourseForEdit(@PathVariable("id") id: Long, @AuthenticationPrincipal userPrincipal: UserPrincipal?): ResponseEntity<CourseDto> {
        ownerService.checkIsAllowedToEdit(id, userPrincipal)
        val course = courseService.getCourseByIdForEdit(id, userPrincipal)
        courseService.clearVariants(course)
        return ResponseEntity.ok(course)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun deleteCourse(@PathVariable("id") id: Long, @AuthenticationPrincipal userPrincipal: UserPrincipal?) {
        ownerService.checkIsAllowedToEdit(id, userPrincipal)
        courseService.deleteCourseById(id)
    }
}
