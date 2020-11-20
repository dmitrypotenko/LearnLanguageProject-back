package com.dpotenko.kirillweb.service

import com.dpotenko.kirillweb.Tables
import com.dpotenko.kirillweb.Tables.LESSON
import com.dpotenko.kirillweb.dto.LessonDto
import com.dpotenko.kirillweb.tables.pojos.CompletedLesson
import com.dpotenko.kirillweb.tables.pojos.Lesson
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class LessonService(val dslContext: DSLContext,
                    val attachmentService: AttachmentService) {

    fun saveLesson(dto: LessonDto,
                   courseId: Long): Long {

        val lesson = Lesson(dto.id, dto.lessonText, dto.name, dto.order, dto.videoLink, courseId, false, null)
        val record = dslContext.newRecord(LESSON, lesson)
        val fieldToChange = record.fields("lesson_text",
                "name",
                "order_number",
                "video_link",
                "course_id",
                "deleted")
        if (dto.id == null) {
            record.insert(*fieldToChange)
        } else {
            record.update(*fieldToChange)
        }

        return record.id
    }

    fun merge(lessons: List<LessonDto>,
              courseId: Long) {
        dslContext.selectFrom(LESSON)
                .where(LESSON.COURSE_ID.eq(courseId).and(LESSON.DELETED.eq(false)))
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

    fun getLessonsByCourseId(courseId: Long): List<LessonDto> {
        val lessons = dslContext.selectFrom(LESSON)
                .where(LESSON.COURSE_ID.eq(courseId).and(LESSON.DELETED.eq(false)))
                .fetchInto(Lesson::class.java)

        val lessonsDto = lessons.map { mapLessonToDto(it) }

        lessonsDto.forEach { lesson -> lesson.attachments = attachmentService.getAttachmentsByLessonId(lesson.id!!) }

        return lessonsDto
    }

    fun getLessonsMetaByCourseId(courseId: Long): List<LessonDto> {
        val lessons = dslContext.select(LESSON.ID, LESSON.COURSE_ID, LESSON.NAME, LESSON.ORDER_NUMBER).from(LESSON)
                .where(LESSON.COURSE_ID.eq(courseId).and(LESSON.DELETED.eq(false)))
                .fetchInto(Lesson::class.java)

        return lessons.map { mapLessonToDto(it) }
    }

    fun getLessonsById(id: Long): LessonDto {
        val lesson = dslContext.selectFrom(LESSON)
                .where(LESSON.ID.eq(id).and(LESSON.DELETED.eq(false)))
                .fetchOneInto(Lesson::class.java)

        return mapLessonToDto(lesson)
    }

    fun markAsCompleted(lessonId: Long,
                        userId: Long) {
        if (getCompletedLesson(userId, lessonId) == null) {
            val completedLesson = CompletedLesson()
            completedLesson.lessonId = lessonId
            completedLesson.userId = userId
            val newRecord = dslContext.newRecord(Tables.COMPLETED_LESSON, completedLesson)
            newRecord.insert()
        }
    }

    fun getCompletedLesson(userId: Long,
                           lessonId: Long): CompletedLesson? {
        return dslContext.selectFrom(Tables.COMPLETED_LESSON.join(LESSON).on(Tables.COMPLETED_LESSON.LESSON_ID.eq(LESSON.ID)))
                .where(LESSON.DELETED.eq(false).and(LESSON.ID.eq(lessonId))
                        .and(Tables.COMPLETED_LESSON.USER_ID.eq(userId)))
                .fetchOneInto(CompletedLesson::class.java)
    }

    private fun mapLessonToDto(lesson: Lesson): LessonDto {
        return LessonDto(
                lesson.videoLink,
                lesson.lessonText?:"",
                lesson.name,
                listOf(),
                lesson.orderNumber.toLong(),
                lesson.id
        )
    }
}
