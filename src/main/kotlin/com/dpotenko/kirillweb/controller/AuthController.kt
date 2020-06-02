package com.dpotenko.kirillweb.controller

import com.dpotenko.kirillweb.domain.UserPrincipal
import com.dpotenko.kirillweb.dto.UserDto
import com.dpotenko.kirillweb.service.OwnerService
import com.dpotenko.kirillweb.service.UserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
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
