package com.dpotenko.kirillweb.service

import com.dpotenko.kirillweb.Tables
import com.dpotenko.kirillweb.dto.LessonDto
import com.dpotenko.kirillweb.tables.pojos.Lesson
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class LessonService(val dslContext: DSLContext,
                    val attachmentService: AttachmentService) {

    fun saveLesson(dto: LessonDto,
                   courseId: Long): Long {

        val lesson = Lesson(dto.id, dto.lessonText, dto.name, dto.order.toInt(), dto.videoLink, courseId, false)
        val record = dslContext.newRecord(Tables.LESSON, lesson)
        if (dto.id == null) {
            record.insert()
        } else {
            record.update()
        }

        return record.id
    }

    fun merge(lessons: List<LessonDto>,
              courseId: Long) {
        dslContext.selectFrom(Tables.LESSON)
                .where(Tables.LESSON.COURSE_ID.eq(courseId))
                .fetch()
                .forEach { lessonRecord ->
                    val foundLesson = lessons.find { it.id == lessonRecord.id }
                    if (foundLesson == null) {
                        lessonRecord.deleted = true
                        lessonRecord.store()
                    } else {
                        attachmentService.merge(foundLesson.attachments, foundLesson.id!!)
                    }
                }
    }
}