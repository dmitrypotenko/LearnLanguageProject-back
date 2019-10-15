package com.dpotenko.kirillweb.config

import com.dpotenko.kirillweb.property.CorsProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import kotlin.math.log


@EnableWebMvc
@Configuration
class SpringMvcConfig(val corsProperties: CorsProperties) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
                .allowedOrigins(corsProperties.origin)
                .allowedHeaders("*")
                .allowCredentials(true)

    }
}



