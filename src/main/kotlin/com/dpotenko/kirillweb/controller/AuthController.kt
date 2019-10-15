package com.dpotenko.kirillweb.controller

import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.RedirectView


@RestController
class AuthController {


    @RequestMapping(path = ["/login"], method = [RequestMethod.OPTIONS])
    fun loginCORS(): RedirectView {
        return RedirectView("login")
    }

    @GetMapping(path = ["/isAdmin"])
    fun isAllowed(@RequestHeader headers: HttpHeaders): String {


        headers.forEach { header -> println(header) }
        val principal = SecurityContextHolder.getContext().authentication.principal as? OAuth2User ?: return "false"

        if (principal.authorities.find { grantedAuthority -> grantedAuthority.authority == "ROLE_ADMIN" } != null) {
            return "true"
        }

        return "false"
    }
}