package com.dpotenko.kirillweb.domain

import com.dpotenko.kirillweb.tables.pojos.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import java.util.Collections

class UserPrincipal(val id: Long,
                    val username: String?,
                    val password: String?,
                    private val authorities: Collection<GrantedAuthority?>,
                    private val oidcUser: OidcUser) : OidcUser {

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
        return oidcUser.attributes
    }

    override fun getName(): String {
        return id.toString()
    }

    override fun getUserInfo(): OidcUserInfo {
        return oidcUser.userInfo
    }

    override fun getIdToken(): OidcIdToken {
        return oidcUser.idToken
    }

    override fun getClaims(): Map<String, Any> {
        return oidcUser.attributes
    }

    companion object {
        fun create(user: User,
                   oidcUser: OidcUser): UserPrincipal {
            val authorities: List<GrantedAuthority> = Collections.singletonList(SimpleGrantedAuthority(user.role))
            return UserPrincipal(
                    user.id,
                    user.email,
                    user.password,
                    authorities,
                    oidcUser
            )
        }
    }

}