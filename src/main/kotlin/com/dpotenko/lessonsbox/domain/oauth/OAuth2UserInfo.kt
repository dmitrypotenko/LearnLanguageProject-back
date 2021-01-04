package com.dpotenko.lessonsbox.domain.oauth

import java.io.Serializable

abstract class OAuth2UserInfo(var attributes: Map<String?, Any?>?): Serializable {

    companion object {
        private const val serialVersionUID = 20180617104402L
    }

    abstract val id: String?
    abstract val name: String?
    abstract val email: String?
    abstract val imageUrl: String?

}
