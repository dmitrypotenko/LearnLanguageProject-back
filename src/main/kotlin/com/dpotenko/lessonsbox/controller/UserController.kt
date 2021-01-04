package com.dpotenko.lessonsbox.controller

import com.dpotenko.lessonsbox.dto.UserAccess
import com.dpotenko.lessonsbox.dto.UserAccessVO
import com.dpotenko.lessonsbox.dto.UserDto
import com.dpotenko.lessonsbox.service.UserService
import com.dpotenko.lessonsbox.service.group.GroupService
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
class UserController(val userService: UserService,
                     val groupService: GroupService) {

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun getAllUsers(): ResponseEntity<List<UserDto>> {
        return ResponseEntity.ok(userService.getAllUsers())
    }

    @GetMapping("/accesses/courses/{courseId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun getSecurityModelForCourse(@PathVariable("courseId") courseId: Long): ResponseEntity<List<UserAccess>> {
        return ResponseEntity.ok(userService.getUserAccesses(courseId))
    }

    @PostMapping("/accesses/courses/{courseId}")
    fun updateCourseAccesses(@PathVariable("courseId") courseId: Long, @RequestBody updateCourseAccessRequest: UpdateCourseAccessRequest) {
        userService.updateCourseAccesses(courseId, updateCourseAccessRequest.accesses)
    }

    @PostMapping("/accesses/groups/{groupId}")
    fun updateGroupAccesses(@PathVariable("groupId") groupId: Long, @RequestBody updateCourseAccessRequest: UpdateCourseAccessRequest) {
        userService.updateGroupAccesses(groupId, updateCourseAccessRequest.accesses)
    }

    @GetMapping("/accesses/groups/{groupId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun getSecurityModelForGroup(@PathVariable("groupId") groupId: Long): ResponseEntity<List<UserAccess>> {
        return ResponseEntity.ok(userService.getGroupAccesses(groupId))
    }

    @GetMapping("/students/groups/{groupId}")
    fun getStudentsForGroup(@PathVariable("groupId") groupId: Long): ResponseEntity<List<UserAccess>> {
        return ResponseEntity.ok(userService.getGroupStudents(groupId))
    }

    @GetMapping("/tests/{testId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun getSubmittedTestUsers(@PathVariable("testId") testId: Long): ResponseEntity<List<UserDto>> {
        return ResponseEntity.ok(userService.getSubmittedTestUsers(testId))
    }
}

data class UpdateCourseAccessRequest(val accesses: List<UserAccessVO>) {}
