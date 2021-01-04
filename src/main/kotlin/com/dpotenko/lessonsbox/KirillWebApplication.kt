package com.dpotenko.lessonsbox

import com.dpotenko.lessonsbox.property.CorsProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.util.TimeZone

import javax.annotation.PostConstruct




@SpringBootApplication
@EnableConfigurationProperties(CorsProperties::class)
@EnableTransactionManagement
class KirillWebApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplicationBuilder()
                    .sources(KirillWebApplication::class.java)
                    .build()
                    .run(*args)
        }
    }

    @PostConstruct
    fun init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

}
