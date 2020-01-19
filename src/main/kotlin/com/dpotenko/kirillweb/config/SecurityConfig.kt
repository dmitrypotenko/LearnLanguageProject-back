package com.dpotenko.kirillweb.config

import com.dpotenko.kirillweb.property.CorsProperties
import com.dpotenko.kirillweb.service.CustomOauth2UserService
import com.dpotenko.kirillweb.service.CustomOauth2UserServiceFacebook
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
class SecurityConfig(val oauth2UserService: CustomOauth2UserService,
                     val customOauth2UserServiceFacebook: CustomOauth2UserServiceFacebook,
                     val corsProperties: CorsProperties) : WebSecurityConfigurerAdapter() {

    @Value("\${app.url}")
    lateinit var appUrl: String

    override fun configure(http: HttpSecurity?) {
        http!!.authorizeRequests()
                .antMatchers("/**")
                .permitAll()
                .and()
                .csrf()  //TODO enable in prod
                .disable()
                .oauth2Client()
                .and()
                .oauth2Login()
                .successHandler(SuccessRedirectHandler(appUrl))
                .userInfoEndpoint()
                .userService(customOauth2UserServiceFacebook)
                .oidcUserService(oauth2UserService)
                .and()
        http.logout()
                .logoutSuccessUrl(appUrl)
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf(corsProperties.origin)
        configuration.allowedMethods = listOf("*")
        configuration.allowCredentials = true
        configuration.allowedHeaders =  listOf("*")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)

        http.cors()
                .configurationSource(source)
    }
}

class SuccessRedirectHandler(val appUrl: String) : SimpleUrlAuthenticationSuccessHandler() {
    override fun onAuthenticationSuccess(request: HttpServletRequest?,
                                         response: HttpServletResponse?,
                                         authentication: Authentication?) {
        redirectStrategy.sendRedirect(request, response, appUrl)
    }

}
