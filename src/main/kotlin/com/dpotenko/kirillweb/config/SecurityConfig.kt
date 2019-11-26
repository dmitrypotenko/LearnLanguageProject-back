package com.dpotenko.kirillweb.config

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
class SecurityConfig : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity?) {
        http!!.authorizeRequests()
                .antMatchers("/**")
                .permitAll()
                .and()
                .csrf()
                .disable()
                .oauth2Client()
                .and()
                .oauth2Login()
                .successHandler(SuccessRedirectHandler())
                .userInfoEndpoint()
                .oidcUserService(oidcUserService())
                .and()
    }

    private fun oidcUserService(): OAuth2UserService<OidcUserRequest, OidcUser> {
        val delegate = OidcUserService()
        return OAuth2UserService { userRequest ->
            var oidcUser = delegate.loadUser(userRequest)
            val email = oidcUser.attributes["email"] as String

            if (email == "gman.dima@googlemail.com" || email == "andrew.golovach25@gmail.com") {
                return@OAuth2UserService DefaultOidcUser(setOf(OAuth2UserAuthority("ROLE_ADMIN", mapOf("" to Any()))), oidcUser.getIdToken(), oidcUser.getUserInfo())
            }
            oidcUser
        }
    }

}

class SuccessRedirectHandler : SimpleUrlAuthenticationSuccessHandler() {
    override fun onAuthenticationSuccess(request: HttpServletRequest?,
                                         response: HttpServletResponse?,
                                         authentication: Authentication?) {


        redirectStrategy.sendRedirect(request, response, "http://localhost:4200/welcome")
    }
}