package com.dpotenko.lessonsbox.domain.oauth

class FacebookUserInfo(attributes: Map<String?, Any?>?) : OAuth2UserInfo(attributes) {
    override val id: String?
        get() = attributes?.get("id") as String?

    override val name: String?
        get() = attributes?.get("name") as String?

    override val email: String?
        get() = attributes?.get("email") as String?

    override val imageUrl: String?
        get() {
            return "https://graph.facebook.com/$id/picture?type=large"
        }
}
