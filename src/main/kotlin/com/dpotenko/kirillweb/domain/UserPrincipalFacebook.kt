package com.dpotenko.kirillweb.domain

import com.dpotenko.kirillweb.tables.pojos.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User
import java.util.*

class UserPrincipalFacebook(val id: Long, val username: String?, val password: String?, private val authorities: Collection<GrantedAuthority?>) : OAuth2User {
    private var attributes: Map<String, Any>? = null

    val isAccountNonExpired: Boolean
        get() = true

    val isAccountNonLocked: Boolean
        get() = true

    val isCredentialsNonExpired: Boolean
        get() = true

    val isEnabled: Boolean
        get() = true

    override fun getAuthorities(): Collection<GrantedAuthority?> {
        return authorities
    }

    override fun getAttributes(): Map<String, Any>? {
        return attributes
    }

    fun setAttributes(attributes: Map<String, Any>?) {
        this.attributes = attributes
    }

    override fun getName(): String {
        return id.toString()
    }

    companion object {
        fun create(user: User): UserPrincipalFacebook {
            val authorities: List<GrantedAuthority> = Collections.singletonList(SimpleGrantedAuthority(user.role))
            return UserPrincipalFacebook(
                    user.id,
                    user.email,
                    user.password,
                    authorities
            )
        }

        fun create(user: User, attributes: Map<String, Any>?): UserPrincipalFacebook {
            val userPrincipal = create(user)
            userPrincipal.setAttributes(attributes)
            return userPrincipal
        }
    }

}