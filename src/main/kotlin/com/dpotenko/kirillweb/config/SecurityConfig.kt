package com.dpotenko.kirillweb.config

import com.dpotenko.kirillweb.property.CorsProperties
import com.dpotenko.kirillweb.service.CustomOauth2UserService
import com.dpotenko.kirillweb.service.CustomOauth2UserServiceFacebook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.security.web.savedrequest.HttpSessionRequestCache
import org.springframework.security.web.savedrequest.RequestCache
import org.springframework.security.web.savedrequest.SavedRequest
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
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
        val requestCache = HttpSessionRequestCache()
        http!!.authorizeRequests()

                .antMatchers("/**")
                .permitAll()
                .and()
                .requestCache().requestCache(requestCache)
                .and()
                .csrf()  //TODO enable in prod
                .disable()
                .oauth2Client()
                .and()
                .oauth2Login()
                .successHandler(SuccessRedirectHandler(appUrl, requestCache))
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
        configuration.allowedHeaders = listOf("*")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)

        http.cors()
                .configurationSource(source)

        http.exceptionHandling()
                .defaultAuthenticationEntryPointFor(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), AntPathRequestMatcher("/**"))
    }

}

class SuccessRedirectHandler(val appUrl: String,val requestCache: RequestCache) : SimpleUrlAuthenticationSuccessHandler() {
    override fun onAuthenticationSuccess(request: HttpServletRequest?,
                                         response: HttpServletResponse?,
                                         authentication: Authentication?) {
        val savedRequest: SavedRequest? = requestCache.getRequest(request, response)
        response?.sendRedirect(if (savedRequest == null) appUrl else savedRequest.getRedirectUrl())
    }

}
