package com.dpotenko.kirillweb.controller

import com.dpotenko.kirillweb.domain.UserPrincipal
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

    @GetMapping(path = ["/isAdmin"])
    fun isAllowed(@AuthenticationPrincipal userPrincipal: UserPrincipal?): String {
        if (userPrincipal != null && userPrincipal.isAdmin()) {
            return "true"
        }

        return "false"
    }
}