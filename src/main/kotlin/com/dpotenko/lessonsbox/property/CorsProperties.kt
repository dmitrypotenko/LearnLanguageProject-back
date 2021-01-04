package com.dpotenko.lessonsbox.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cors")
class CorsProperties {

    lateinit var origin: String
}
