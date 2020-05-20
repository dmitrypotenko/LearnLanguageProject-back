package com.dpotenko.kirillweb.controller

import com.dpotenko.kirillweb.dto.UserAccess
import com.dpotenko.kirillweb.dto.UserAccessVO
import com.dpotenko.kirillweb.dto.UserDto
import com.dpotenko.kirillweb.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(val userService: UserService) {

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun getAllUsers(): ResponseEntity<List<UserDto>> {
        return ResponseEntity.ok(userService.getAllUsers())
    }

    @GetMapping("/accesses/{courseId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun getSecurityModelForCourse(@PathVariable("courseId") courseId: Long): ResponseEntity<List<UserAccess>> {
        return ResponseEntity.ok(userService.getUserAccesses(courseId))
    }

    @PostMapping("/accesses/{courseId}")
    fun updateCourseAccesses(@PathVariable("courseId") courseId: Long, @RequestBody updateCourseAccessRequest: UpdateCourseAccessRequest) {
        userService.updateCourseAccesses(courseId, updateCourseAccessRequest.accesses)
    }

    @GetMapping("/tests/{testId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun getSubmittedTestUsers(@PathVariable("testId") testId: Long): ResponseEntity<List<UserDto>> {
        return ResponseEntity.ok(userService.getSubmittedTestUsers(testId))
    }
}

data class UpdateCourseAccessRequest(val accesses: List<UserAccessVO>) {}
