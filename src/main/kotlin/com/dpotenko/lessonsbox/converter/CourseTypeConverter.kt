package com.dpotenko.lessonsbox.converter

import com.dpotenko.lessonsbox.dto.CourseType
import org.jooq.Converter

class CourseTypeConverter : Converter<String, CourseType> {
    override fun from(databaseObject: String?): CourseType? {
        return databaseObject?.let { CourseType.valueOf(databaseObject) }
    }

    override fun to(userObject: CourseType?): String? {
        return userObject?.name
    }

    override fun fromType(): Class<String> {
        return String::class.java
    }

    override fun toType(): Class<CourseType> {
        return CourseType::class.java
    }
}
