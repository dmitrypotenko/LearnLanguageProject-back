package com.dpotenko.kirillweb.controller

import com.dpotenko.kirillweb.domain.UserPrincipal
import com.dpotenko.kirillweb.dto.UserDto
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView


@RestController
class AuthController {


    @RequestMapping(path = ["/login"], method = [RequestMethod.OPTIONS])
    fun loginCORS(): RedirectView {
        return RedirectView("login")
    }

    @GetMapping(path = ["/userInfo"])
    fun getUser(@AuthenticationPrincipal userPrincipal: UserPrincipal?): UserDto {
        return UserDto(
                userPrincipal?.id,
                userPrincipal?.authorities?.map { it?.authority ?: "" } ?: listOf()
        )
    }
}
