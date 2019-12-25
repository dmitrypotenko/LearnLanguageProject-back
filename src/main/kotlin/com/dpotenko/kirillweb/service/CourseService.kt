package com.dpotenko.kirillweb.service

import com.dpotenko.kirillweb.Tables
import com.dpotenko.kirillweb.dto.CompletionDto
import com.dpotenko.kirillweb.dto.CourseDto
import com.dpotenko.kirillweb.tables.pojos.CompletedLesson
import com.dpotenko.kirillweb.tables.pojos.CompletedTest
import com.dpotenko.kirillweb.tables.pojos.Course
import com.dpotenko.kirillweb.tables.pojos.StartedCourse
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


    fun getAllCourses(userId: Long?): List<CourseDto> {
        val courses = dslContext.selectFrom(Tables.COURSE)
                .where(Tables.COURSE.DELETED.eq(false))
                .fetchInto(Course::class.java)
        courses.forEach {
        }
        val dtos = courses.map {
            mapCourseToDto(it)
        }
        dtos.forEach { setCompletion(it, userId) }
        return dtos
    }

    fun getCourseById(id: Long,
                      userId: Long?): CourseDto {
        val courseDto = getCourseByIdForEdit(id)
        setCompletion(courseDto, userId)
        userId?.let { markAsStarted(userId, courseDto.id!!) }
        courseDto.tests.forEach { test ->
            if (userId?.let { testService.getCompletedTest(userId, test.id!!) != null } == true) {
                test.isCompleted = true
                for (question in test.questions) {
                    variantService.getChosenVariantsForQuestion(question.id!!, userId).forEach { variant -> question.variants.find { it.id == variant.id }?.isTicked = true }
                }
                testService.checkTest(test)
            } else {
                test.questions.forEach { question ->
                    question.variants.forEach {
                        it.isRight = false
                    }
                }
            }

        }
        courseDto.lessons.forEach { lesson ->
            if (userId?.let { lessonService.getCompletedLesson(userId, lesson.id!!) != null } == true) {
                lesson.isCompleted = true
            }
        }


        return courseDto
    }

    fun getCourseByIdForEdit(id: Long): CourseDto {
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

    private fun markAsStarted(userId: Long,
                              courseId: Long) {
        val startedCourse = getStartedCourse(courseId, userId)

        if (startedCourse == null) {
            val toInsert = StartedCourse()
            toInsert.userId = userId
            toInsert.courseId = courseId
            val newRecord = dslContext.newRecord(Tables.STARTED_COURSE, toInsert)
            newRecord.insert()
        }

    }

    private fun setCompletion(courseDto: CourseDto,
                              userId: Long?) {
        val startedCourse = getStartedCourse(courseDto.id!!, userId)

        val lessons = lessonService.getLessonsByCourseId(courseDto.id!!)
        val tests = testService.getTestsByCourseId(courseDto.id!!)

        val completedLessons = dslContext.selectFrom(Tables.LESSON.leftJoin(Tables.COMPLETED_LESSON).on(Tables.COMPLETED_LESSON.LESSON_ID.eq(Tables.LESSON.ID)))
                .where(Tables.LESSON.DELETED.eq(false).and(Tables.LESSON.COURSE_ID.eq(courseDto.id)).and(Tables.COMPLETED_LESSON.USER_ID.eq(userId)))
                .fetchInto(CompletedLesson::class.java)

        val completedTests = dslContext.selectFrom(Tables.COMPLETED_TEST.join(Tables.TEST).on(Tables.COMPLETED_TEST.TEST_ID.eq(Tables.TEST.ID)))
                .where(Tables.TEST.DELETED.eq(false).and(Tables.TEST.COURSE_ID.eq(courseDto.id)).and(Tables.COMPLETED_TEST.USER_ID.eq(userId)))
                .fetchInto(CompletedTest::class.java)

        val allLessonsCompleted = lessons.all { lessonDto -> completedLessons.find { it.lessonId == lessonDto.id } != null }
        val allTestsCompleted = tests.all { testDto -> completedTests.find { it.testId == testDto.id } != null }

        courseDto.completion = CompletionDto(
                startedCourse != null,
                allLessonsCompleted && allTestsCompleted && (lessons.isNotEmpty() || tests.isNotEmpty()),
                0.0,
                0.0
        )
    }

    private fun getStartedCourse(courseId: Long,
                                 userId: Long?): StartedCourse? {
        val startedCourse = dslContext.selectFrom(Tables.STARTED_COURSE.join(Tables.COURSE).on(Tables.STARTED_COURSE.COURSE_ID.eq(Tables.COURSE.ID)))
                .where(Tables.COURSE.DELETED.eq(false).and(Tables.COURSE.ID.eq(courseId))
                        .and(Tables.STARTED_COURSE.USER_ID.eq(userId)))
                .fetchOneInto(StartedCourse::class.java)
        return startedCourse
    }

    fun deleteCourseById(courseId: Long) {
        dslContext.update(Tables.COURSE)
                .set(Tables.COURSE.DELETED, true)
                .where(Tables.COURSE.ID.eq(courseId))
                .execute()
    }
}

/*
*
        val completedLessons = dslContext.fetchCount(dslContext.selectFrom(Tables.LESSON.leftJoin(Tables.COMPLETED_LESSON.`as`("cl")).on(Tables.COMPLETED_LESSON.LESSON_ID.eq(Tables.LESSON.ID)).and(Tables.COMPLETED_LESSON.USER_ID.eq(userId)))
                .where(Tables.LESSON.DELETED.eq(false).and(Tables.LESSON.COURSE_ID.eq(courseDto.id)).and(Tables.COMPLETED_LESSON.ID.isNull()).and(Tables.LESSON.ID.isNotNull())))

        val completedTests = dslContext.fetchCount(dslContext.selectFrom(Tables.TEST.leftJoin(Tables.COMPLETED_TEST.`as`("ct")).on(Tables.COMPLETED_TEST.TEST_ID.eq(Tables.TEST.ID)).and(Tables.COMPLETED_TEST.USER_ID.eq(userId)))
                .where(Tables.TEST.DELETED.eq(false).and(Tables.TEST.COURSE_ID.eq(courseDto.id)).and(Tables.COMPLETED_TEST.ID.isNull()).and(Tables.TEST.ID.isNotNull())))

        val allLessonsCompleted = completedLessons == 0
        val allTestsCompleted = completedTests == 0

        courseDto.completion = CompletionDto(
                startedCourse != null,
                allLessonsCompleted && allTestsCompleted ,
                0.0,
                0.0
        )*/