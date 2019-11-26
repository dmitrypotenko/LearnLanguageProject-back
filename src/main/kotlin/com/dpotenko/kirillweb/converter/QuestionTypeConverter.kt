package com.dpotenko.kirillweb.converter

import com.dpotenko.kirillweb.dto.QuestionType
import org.jooq.Converter

class QuestionTypeConverter : Converter<String, QuestionType> {
    override fun from(databaseObject: String?): QuestionType? {
        return databaseObject?.let { QuestionType.valueOf(databaseObject) }
    }

    override fun to(userObject: QuestionType?): String? {
        return userObject?.name
    }

    override fun fromType(): Class<String> {
        return String::class.java
    }

    override fun toType(): Class<QuestionType> {
        return QuestionType::class.java
    }
}
