package com.dpotenko.kirillweb.service

import com.dpotenko.kirillweb.Tables
import com.dpotenko.kirillweb.dto.QuestionDto
import com.dpotenko.kirillweb.tables.pojos.Question
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class QuestionService(val dslContext: DSLContext) {
    fun saveQuestion(dto: QuestionDto,
                     testId: Long): Long {
        val record = dslContext.newRecord(Tables.QUESTION, Question(dto.question, dto.type, dto.id, testId))
        if (dto.id == null) {
            record.insert()
        } else {
            record.update()
        }

        return record.id
    }

}