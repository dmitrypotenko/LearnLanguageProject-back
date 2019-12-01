package com.dpotenko.kirillweb.service

import com.dpotenko.kirillweb.Tables
import com.dpotenko.kirillweb.dto.CourseDto
import com.dpotenko.kirillweb.tables.pojos.Course
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class CourseService(val lessonService: LessonService,
                    val questionService: QuestionService,
                    val testService: TestService,
                    val variantService: VariantService,
                    val attachmentService: AttachmentService,
                    val dslContext: DSLContext) {
    fun saveCourse(dto: CourseDto): CourseDto {
        val course = dslContext.newRecord(Tables.COURSE, Course(dto.name, dto.category, dto.description, dto.id, false))

        if (dto.id == null || dto.id == 0L) {
            course.insert()
        } else {
            course.update()
        }
        dto.id = course.id
        dto.lessons.forEach { lesson ->
            lesson.id = lessonService.saveLesson(lesson, course.id)
            lesson.attachments.forEach { attachmentDto ->
                attachmentDto.id = attachmentService.saveAttachment(attachmentDto, lesson.id!!)
            }
        }

        dto.tests.forEach { it.id = testService.saveTest(it, course.id) }
        dto.tests.forEach { testDto ->
            testDto.questions.forEach { questionDto ->
                questionDto.id = questionService.saveQuestion(questionDto, testDto.id!!)
                questionDto.variants.forEach { variantDto -> variantDto.id = variantService.saveVariant(variantDto, questionDto.id!!) }
            }
        }

        lessonService.merge(dto.lessons, course.id)
        testService.merge(dto.tests, course.id)

        return dto
    }


    fun getAllCourses(): List<CourseDto> {
        val courses = dslContext.selectFrom(Tables.COURSE)
                .where(Tables.COURSE.DELETED.eq(false))
                .fetchInto(Course::class.java)
        return courses.map { mapCourseToDto(it) }
    }

    fun getCourseById(id: Long): CourseDto {
        val course = dslContext.selectFrom(Tables.COURSE)
                .where(Tables.COURSE.DELETED.eq(false).and(Tables.COURSE.ID.eq(id)))
                .fetchOneInto(Course::class.java)
        val dto = mapCourseToDto(course)

        dto.lessons = lessonService.getLessonsByCourseId(id)
        dto.tests = testService.getTestsByCourseId(id)

        return dto
    }

    private fun mapCourseToDto(course: Course): CourseDto {
        return CourseDto(
                course.name,
                course.description,
                course.category,
                emptyList(),
                emptyList(),
                course.id
        )
    }

    fun deleteCourseById(courseId: Long) {
        dslContext.update(Tables.COURSE)
                .set(Tables.COURSE.DELETED, true)
                .where(Tables.COURSE.ID.eq(courseId))
                .execute()
    }
}