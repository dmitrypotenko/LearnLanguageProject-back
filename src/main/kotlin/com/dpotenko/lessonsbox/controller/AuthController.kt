package com.dpotenko.lessonsbox.controller

import com.dpotenko.lessonsbox.domain.UserPrincipal
import com.dpotenko.lessonsbox.dto.UserDto
import com.dpotenko.lessonsbox.service.UserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView


@RestController
class AuthController(private val userService: UserService) {


    @RequestMapping(path = ["/login"], method = [RequestMethod.OPTIONS])
    fun loginCORS(): RedirectView {
        return RedirectView("login")
    }

    @GetMapping(path = ["/userInfo"])
    fun getUser(@AuthenticationPrincipal userPrincipal: UserPrincipal?): UserDto {
        return userPrincipal?.id?.let { userService.getUserInfo(it) } ?: UserDto(null, "", "", emptyList())
    }
}
