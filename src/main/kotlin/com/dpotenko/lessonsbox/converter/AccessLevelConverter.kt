package com.dpotenko.lessonsbox.converter

import com.dpotenko.lessonsbox.dto.AccessLevel
import org.jooq.Converter

class AccessLevelConverter : Converter<String, AccessLevel> {
    override fun from(databaseObject: String?): AccessLevel? {
        return databaseObject?.let { AccessLevel.valueOf(databaseObject) } ?: AccessLevel.NONE
    }

    override fun to(userObject: AccessLevel?): String? {
        return userObject?.name
    }

    override fun fromType(): Class<String> {
        return String::class.java
    }

    override fun toType(): Class<AccessLevel> {
        return AccessLevel::class.java
    }
}
