package com.dpotenko.kirillweb.converter

import com.dpotenko.kirillweb.dto.CourseAccessLevel
import org.jooq.Converter

class CourseAccessLevelConverter : Converter<String, CourseAccessLevel> {
    override fun from(databaseObject: String?): CourseAccessLevel? {
        return databaseObject?.let { CourseAccessLevel.valueOf(databaseObject) } ?: CourseAccessLevel.NONE
    }

    override fun to(userObject: CourseAccessLevel?): String? {
        return userObject?.name
    }

    override fun fromType(): Class<String> {
        return String::class.java
    }

    override fun toType(): Class<CourseAccessLevel> {
        return CourseAccessLevel::class.java
    }
}
