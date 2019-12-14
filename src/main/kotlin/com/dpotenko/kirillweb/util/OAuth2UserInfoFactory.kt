package com.dpotenko.kirillweb.util


import com.dpotenko.kirillweb.domain.oauth.FacebookUserInfo
import com.dpotenko.kirillweb.domain.oauth.GoogleUserInfo
import com.dpotenko.kirillweb.domain.oauth.OAuth2UserInfo
import javax.naming.AuthenticationException


class OAuth2UserInfoFactory {

    companion object {
        fun getOAuth2UserInfo(registrationId: String, attributes: Map<String?, Any?>?): OAuth2UserInfo? {
            return when {
                registrationId.equals("google", ignoreCase = true) -> {
                    GoogleUserInfo(attributes)
                }
                registrationId.equals("facebook", ignoreCase = true) -> {
                    FacebookUserInfo(attributes)
                }
                else -> {
                    throw AuthenticationException("Sorry! Login with $registrationId is not supported yet.")
                }
            }
        }
    }
}