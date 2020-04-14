package com.dpotenko.kirillweb.domain

import com.dpotenko.kirillweb.tables.pojos.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import java.util.Collections

class OidUserPrincipal(id: Long,
                       username: String?,
                       password: String?,
                       authorities: Collection<GrantedAuthority?>,
                       private val oidcUser: OidcUser) : OidcUser, UserPrincipal(id, username, password, authorities) {

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
                   oidcUser: OidcUser): OidUserPrincipal {
            val authorities: List<GrantedAuthority> = user.role.split(",").map { SimpleGrantedAuthority(it) }
            return OidUserPrincipal(
                    user.id,
                    user.email,
                    user.password,
                    authorities,
                    oidcUser
            )
        }
    }

}
