package com.dpotenko.kirillweb.controller

import com.dpotenko.kirillweb.dto.TestDto
import com.dpotenko.kirillweb.service.TestService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/tests")
class TestController(val testService: TestService) {
    @PostMapping("/check")
    fun checkTest(@RequestBody testDto: TestDto): ResponseEntity<TestDto> {
        return ResponseEntity.ok(testService.checkTest(testDto))
    }
}