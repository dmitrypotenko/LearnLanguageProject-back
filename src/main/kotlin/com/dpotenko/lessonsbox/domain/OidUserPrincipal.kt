package com.dpotenko.lessonsbox.domain

import com.dpotenko.lessonsbox.tables.pojos.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import java.io.Serializable

class OidUserPrincipal(id: Long,
                       username: String?,
                       password: String?,
                       authorities: Collection<GrantedAuthority?>,
                       private val oidcUser: OidcUser) : OidcUser, UserPrincipal(id, username, password, authorities), Serializable {


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
        private const val serialVersionUID = 20180617104400L

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
