package com.dpotenko.kirillweb.config

import org.springframework.security.core.context.SecurityContext
import org.springframework.security.web.context.HttpRequestResponseHolder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.stereotype.Component
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.HttpServletResponse

        //@Component
class SecurityContextFetcher() : SecurityContextRepository {
    private val httpSessionSecurityContextRepository: HttpSessionSecurityContextRepository = HttpSessionSecurityContextRepository()

    override fun loadContext(requestResponseHolder: HttpRequestResponseHolder?): SecurityContext {
        return httpSessionSecurityContextRepository.loadContext(HttpRequestResponseHolder(wrapRequest(requestResponseHolder?.request), requestResponseHolder?.response))
    }

    override fun saveContext(context: SecurityContext?,
                             request: HttpServletRequest?,
                             response: HttpServletResponse?) {
        httpSessionSecurityContextRepository.saveContext(context, request, response)
    }

    override fun containsContext(request: HttpServletRequest?): Boolean {
        return httpSessionSecurityContextRepository.containsContext(wrapRequest(request))
    }

    private fun wrapRequest(request: HttpServletRequest?): HttpServletRequestWrapper {
        return object : HttpServletRequestWrapper(request) {
            override fun getCookies(): Array<Cookie> {
                val cookies = mutableListOf(*super.getCookies())
                val header = request?.getHeader("Authentication")
                if (header?.isNotBlank() ?: false) {
                    cookies.add(Cookie("JSESSIONID", header))
                }

                return arrayOf(*super.getCookies())
            }
        }
    }
}
