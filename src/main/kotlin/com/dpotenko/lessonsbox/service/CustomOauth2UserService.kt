package com.dpotenko.lessonsbox.service

import com.dpotenko.lessonsbox.Tables
import com.dpotenko.lessonsbox.domain.OAuth2AuthenticationProcessingException
import com.dpotenko.lessonsbox.domain.OidUserPrincipal
import com.dpotenko.lessonsbox.domain.oauth.OAuth2UserInfo
import com.dpotenko.lessonsbox.tables.pojos.User
import com.dpotenko.lessonsbox.util.OAuth2UserInfoFactory
import org.jooq.DSLContext
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils

@Component
class CustomOauth2UserService(private val dslContext: DSLContext) : OAuth2UserService<OidcUserRequest, OidcUser> {
    private val delegate: OidcUserService = OidcUserService()

    override fun loadUser(userRequest: OidcUserRequest?): OidcUser {

        val oAuth2User = delegate.loadUser(userRequest)

        return try {
            processOAuth2User(userRequest!!, oAuth2User)
        } catch (ex: AuthenticationException) {
            throw ex
        } catch (ex: Exception) { // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            ex.printStackTrace()
            throw InternalAuthenticationServiceException(ex.message, ex.cause)
        }
    }

    private fun processOAuth2User(oAuth2UserRequest: OAuth2UserRequest,
                                  oidcUser: OidcUser): OidcUser {
        val oAuth2UserInfo: OAuth2UserInfo? =
                OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.clientRegistration.registrationId, oidcUser.attributes)
        if (StringUtils.isEmpty(oAuth2UserInfo?.email)) {
            throw OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider")
        }

        var user: User? = findUserByEmail(oAuth2UserInfo?.email)
        if (user != null) {
            if (!user.authProvider.equals(oAuth2UserRequest.clientRegistration.registrationId)) {
                throw OAuth2AuthenticationProcessingException("Looks like you're signed up with " + user.authProvider
                        + " account. Please use your " + user.authProvider + " account to login.")
            }
            user = updateExistingUser(user, oAuth2UserInfo)
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo)
        }

        return user?.let { OidUserPrincipal.create(it, oidcUser) }!!
    }

    private fun registerNewUser(oAuth2UserRequest: OAuth2UserRequest,
                                oAuth2UserInfo: OAuth2UserInfo?): User? {
        val user = User(null, oAuth2UserInfo?.name, oAuth2UserInfo?.email, null, oAuth2UserRequest.clientRegistration
                .registrationId, oAuth2UserInfo?.id, "ROLE_USER,ROLE_ADMIN", oAuth2UserInfo?.imageUrl)

        val record = dslContext.newRecord(Tables.USER, user)
        record.insert()

        user.id = record.id
        return user
    }

    private fun updateExistingUser(existingUser: User,
                                   oAuth2UserInfo: OAuth2UserInfo?): User? {
        existingUser.name = oAuth2UserInfo?.name
        existingUser.imageurl = oAuth2UserInfo?.imageUrl

        dslContext.newRecord(Tables.USER, existingUser).update()

        return existingUser
    }

    private fun findUserByEmail(email: String?): User? {
        return dslContext.selectFrom(Tables.USER)
                .where(Tables.USER.EMAIL.eq(email))
                .fetchOneInto(User::class.java)
    }
}
