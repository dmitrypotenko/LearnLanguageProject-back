package com.dpotenko.kirillweb.domain.oauth

class FacebookUserInfo(attributes: Map<String?, Any?>?) : OAuth2UserInfo(attributes) {
    override val id: String?
        get() = attributes?.get("id") as String?

    override val name: String?
        get() = attributes?.get("name") as String?

    override val email: String?
        get() = attributes?.get("email") as String?

    override val imageUrl: String?
        get() {
            if (attributes?.containsKey("picture")!!) {
                val pictureObj = attributes?.get("picture") as Map<*, *>?
                if (pictureObj!!.containsKey("data")) {
                    val dataObj = pictureObj["data"] as Map<*, *>?
                    if (dataObj!!.containsKey("url")) {
                        return dataObj["url"] as String?
                    }
                }
            }
            return null
        }
}