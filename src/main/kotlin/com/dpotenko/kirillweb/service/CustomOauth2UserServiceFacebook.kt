package com.dpotenko.kirillweb.service

import com.dpotenko.kirillweb.Tables
import com.dpotenko.kirillweb.domain.OAuth2AuthenticationProcessingException
import com.dpotenko.kirillweb.domain.UserPrincipal
import com.dpotenko.kirillweb.domain.oauth.OAuth2UserInfo
import com.dpotenko.kirillweb.tables.pojos.User
import com.dpotenko.kirillweb.util.OAuth2UserInfoFactory
import org.jooq.DSLContext
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils

@Component
class CustomOauth2UserServiceFacebook(private val dslContext: DSLContext) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private val delegate: DefaultOAuth2UserService = DefaultOAuth2UserService()

    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User {
        val oAuth2User = delegate.loadUser(userRequest)

        return try {
            processOAuth2User(userRequest!!, oAuth2User)
        } catch (ex: AuthenticationException) {
            throw ex
        } catch (ex: Exception) { // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw InternalAuthenticationServiceException(ex.message, ex.cause)
        }
    }

    private fun processOAuth2User(oAuth2UserRequest: OAuth2UserRequest, oAuth2User: OAuth2User): OAuth2User {
        val oAuth2UserInfo: OAuth2UserInfo? =
                OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.clientRegistration.registrationId, oAuth2User.attributes)
        if (StringUtils.isEmpty(oAuth2UserInfo?.id)) {
            throw OAuth2AuthenticationProcessingException("Facebook id not found from OAuth2 provider")
        }

        var user: User? = findUserByFacebookId(oAuth2UserInfo?.id)
        if (user != null) {
            if (!user.authProvider.equals(oAuth2UserRequest.clientRegistration.registrationId)) {
                throw OAuth2AuthenticationProcessingException("Looks like you're signed up with " + user.authProvider
                        + " account. Please use your " + user.authProvider + " account to login.")
            }
            user = updateExistingUser(user, oAuth2UserInfo)
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo)
        }

        return user?.let { UserPrincipal.create(it, oAuth2User.attributes) }!!
    }

    private fun registerNewUser(oAuth2UserRequest: OAuth2UserRequest, oAuth2UserInfo: OAuth2UserInfo?): User? {
        val user = User(null, oAuth2UserInfo?.name, oAuth2UserInfo?.email, null, oAuth2UserRequest.clientRegistration
                .registrationId, oAuth2UserInfo?.id, "ROLE_USER", oAuth2UserInfo?.imageUrl)

        val record = dslContext.newRecord(Tables.USER, user)
        record.insert()

        user.id = record.id
        return user
    }

    private fun updateExistingUser(existingUser: User, oAuth2UserInfo: OAuth2UserInfo?): User? {
        existingUser.name = oAuth2UserInfo?.name
        existingUser.imageurl = oAuth2UserInfo?.imageUrl

        dslContext.newRecord(Tables.USER, existingUser).update()

        return existingUser
    }

    private fun findUserByFacebookId(facebookId: String?): User? {
        return dslContext.selectFrom(Tables.USER)
                .where(Tables.USER.AUTH_PROVIDER_ID.eq(facebookId))
                .fetchOneInto(User::class.java)
    }
}