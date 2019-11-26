package com.dpotenko.kirillweb.service

import com.dpotenko.kirillweb.Tables
import com.dpotenko.kirillweb.dto.LessonDto
import com.dpotenko.kirillweb.tables.pojos.Lesson
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class LessonService(val dslContext: DSLContext) {

    fun saveLesson(dto: LessonDto,
                   courseId: Long): Long {

        val lesson = Lesson(dto.id, dto.lessonText, dto.name, dto.order.toInt(), dto.videoLink, courseId)
        val record = dslContext.newRecord(Tables.LESSON, lesson)
        if (dto.id == null) {
            record.insert()
        } else {
            record.update()
        }

        return record.id
    }
}