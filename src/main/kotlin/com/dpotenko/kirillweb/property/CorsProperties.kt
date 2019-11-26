package com.dpotenko.kirillweb.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cors")
class CorsProperties {

    lateinit var origin: String
}
