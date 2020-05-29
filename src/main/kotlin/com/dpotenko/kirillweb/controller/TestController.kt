package com.dpotenko.kirillweb.controller

import com.dpotenko.kirillweb.domain.UserPrincipal
import com.dpotenko.kirillweb.dto.TestDto
import com.dpotenko.kirillweb.dto.UserDto
import com.dpotenko.kirillweb.service.OwnerService
import com.dpotenko.kirillweb.service.TestService
import com.dpotenko.kirillweb.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/tests")
class TestController(val testService: TestService,
                     val userService: UserService,
                     val ownerService: OwnerService) {

    @PostMapping("/check")
    fun checkTest(@RequestBody testDto: TestDto, @AuthenticationPrincipal userPrincipal: UserPrincipal?): ResponseEntity<TestDto> {
        val completedTest = userPrincipal?.let { testService.getCompletedTest(it.id, testDto.id!!) }
        val testById = testService.getTestById(testDto.id!!)
        ownerService.checkAllowed(testById, userPrincipal)
        if (completedTest != null && !testById.isRetryable) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "This test is already completed and you are not allowed to retry it.")
        }
        val checkedTest = testService.checkTest(testDto)
        checkedTest.isCompleted = true
        userPrincipal?.let { testService.markAsCompleted(completedTest, testDto, it.id) }
        return ResponseEntity.ok(checkedTest)
    }

    @PostMapping("/invalidate/{testId}")
    fun invalidateTest(@PathVariable("testId") testId: Long, @AuthenticationPrincipal userPrincipal: UserPrincipal?): ResponseEntity<Unit> {
        val testById = testService.getTestById(testId)
        ownerService.checkAllowed(testById, userPrincipal)
        userPrincipal?.let { testService.invalidateTest(testById, it.id) }
        return ResponseEntity.ok().build<Unit>()
    }

    @GetMapping("/check/{testId}/users/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun getCheckedTest(@PathVariable("testId") testId: Long, @PathVariable("userId") userId: Long): ResponseEntity<TestDto> {
        val testDto = testService.getFullTestById(testId)
        testService.checkTestForUser(testDto, userId)
        return ResponseEntity.ok(testDto)
    }


    @PostMapping("/invalidate/{testId}/users/{userId}")
    fun invalidateTestForUser(@PathVariable("testId") testId: Long, @PathVariable("userId") userId: Long,  @AuthenticationPrincipal userPrincipal: UserPrincipal?): ResponseEntity<Unit> {
        val testById = testService.getTestById(testId)
        ownerService.checkIsAllowedToEdit(testById, userPrincipal)
        testService.invalidateTest(testById, userId)
        return ResponseEntity.ok().build<Unit>()
    }

}
