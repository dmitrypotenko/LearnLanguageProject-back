package com.dpotenko.kirillweb

import com.dpotenko.kirillweb.config.beans
import com.dpotenko.kirillweb.property.CorsProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.EnableConfigurationProperties

@SpringBootApplication
@EnableConfigurationProperties(CorsProperties::class)
class KirillwebApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplicationBuilder()
                    .initializers(beans)
                    .sources(KirillwebApplication::class.java)
                    .build()
                    .run(*args)
        }
    }

}
