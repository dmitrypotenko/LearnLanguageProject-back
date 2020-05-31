package com.dpotenko.kirillweb.service

import com.dpotenko.kirillweb.Tables.CHOSEN_VARIANT
import com.dpotenko.kirillweb.Tables.COMPLETED_TEST
import com.dpotenko.kirillweb.Tables.TEST
import com.dpotenko.kirillweb.Tables.VARIANT
import com.dpotenko.kirillweb.dto.QuestionDto
import com.dpotenko.kirillweb.dto.QuestionStatus
import com.dpotenko.kirillweb.dto.QuestionType
import com.dpotenko.kirillweb.dto.TestDto
import com.dpotenko.kirillweb.dto.VariantDto
import com.dpotenko.kirillweb.service.question.CustomInputViewTransformer
import com.dpotenko.kirillweb.service.question.QuestionChecker
import com.dpotenko.kirillweb.tables.pojos.CompletedTest
import com.dpotenko.kirillweb.tables.pojos.Test
import com.dpotenko.kirillweb.tables.pojos.Variant
import org.jooq.DSLContext
import org.jsoup.Jsoup
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class TestService(val dslContext: DSLContext,
                  val questionService: QuestionService,
                  val variantService: VariantService,
                  val questionChekers: List<QuestionChecker>,
                  val viewTransformers: List<CustomInputViewTransformer>) {

    fun saveTest(dto: TestDto,
                 courseId: Long): Long {
        val record = dslContext.newRecord(TEST, Test(dto.id, dto.name, dto.order, courseId, false, dto.successThreshold.toLong(), dto.isRetryable, dto.instruction))
        if (dto.id == null) {
            record.insert()
        } else {
            record.update()
        }

        return record.id
    }


    fun merge(tests: List<TestDto>,
              courseId: Long) {
        dslContext.selectFrom(TEST)
                .where(TEST.COURSE_ID.eq(courseId).and(TEST.DELETED.eq(false)))
                .fetch()
                .forEach { testRecord ->
                    val foundTest = tests.find { it.id == testRecord.id }
                    if (foundTest == null) {
                        testRecord.deleted = true
                        testRecord.store()
                    } else {
                        questionService.merge(foundTest.questions, foundTest.id!!)
                    }
                }
    }

    fun getFullTestsByCourseId(courseId: Long): List<TestDto> {
        val testsDto = getTestsByCourseId(courseId)

        testsDto.forEach { testDto -> testDto.questions = questionService.getQuestionsByTestId(testDto.id!!) }

        return testsDto
    }

    fun getTestsByCourseId(courseId: Long): List<TestDto> {
        val tests = dslContext.selectFrom(TEST)
                .where(TEST.COURSE_ID.eq(courseId).and(TEST.DELETED.eq(false)))
                .fetchInto(Test::class.java)

        val testsDto = tests.map { mapTestToDto(it) }
        return testsDto
    }

    fun checkTest(userTestDto: TestDto,
                  testDto: TestDto = getTestById(userTestDto.id!!)): TestDto {
        testDto.questions = questionService.getQuestionsByTestId(testDto.id!!)

        userTestDto.questions.forEach { userQuestion ->
            testDto.questions.find { questionDto -> questionDto.id == userQuestion.id }?.let { questionDto ->
                if (questionChekers.find { it.types().contains(userQuestion.type) }?.checkQuestion(userQuestion, questionDto)
                                ?: throw IllegalArgumentException("Can't find checker")) {
                    userQuestion.status = QuestionStatus.SUCCESS
                } else {
                    userQuestion.status = QuestionStatus.FAILED
                }
            }
        }

        return userTestDto
    }


    fun markAsCompleted(existingRecord: CompletedTest?,
                        testDto: TestDto,
                        userId: Long) {
        if (existingRecord == null) {
            val completedTest = CompletedTest()
            completedTest.userId = userId
            completedTest.testId = testDto.id
            val newRecord = dslContext.newRecord(COMPLETED_TEST, completedTest)
            newRecord.insert()
            testDto.questions.forEach { question ->
                question.variants.filter { it.isTicked }
                        .forEach {
                            if (it.inputType == "input") {
                                val variantDto = VariantDto(it.variant, it.isRight, it.isWrong, it.isTicked, it.explanation, null, it.inputName, it.inputType)
                                variantDto.id = variantService.saveVariant(variantDto.copy(isRight = false), questionId = question.id!!, questionType = question.type)
                                variantService.markAsChosenVariant(userId, variantDto)
                            } else {
                                variantService.markAsChosenVariant(userId, it)
                            }
                        }
            }
        }
    }

    fun getCompletedTest(userId: Long,
                         testId: Long): CompletedTest? {
        return dslContext.selectFrom(COMPLETED_TEST.join(TEST).on(COMPLETED_TEST.TEST_ID.eq(TEST.ID)))
                .where(TEST.DELETED.eq(false).and(TEST.ID.eq(testId))
                        .and(COMPLETED_TEST.USER_ID.eq(userId)))
                .fetchOneInto(CompletedTest::class.java)
    }

    private fun mapTestToDto(test: Test): TestDto {
        return TestDto(
                listOf(),
                test.name,
                test.instruction,
                test.orderNumber.toLong(),
                test.id,
                test.successThreshold.toInt(),
                test.retryable
        )
    }

    fun getTestById(testId: Long): TestDto {
        val test = dslContext.selectFrom(TEST)
                .where(TEST.ID.eq(testId).and(TEST.DELETED.eq(false)))
                .fetchOneInto(Test::class.java)



        return mapTestToDto(test)

    }

    fun fillInTest(testDto: TestDto): TestDto {

        testDto.questions = questionService.getQuestionsByTestId(testDto.id!!)
        transformInputFields(testDto)

        return testDto

    }

    fun fillInTestWithUserQuestions(testDto: TestDto, userId: Long): TestDto {

        testDto.questions = questionService.getUserVersionQuestions(testDto.id!!, userId)
        transformInputFields(testDto)

        return testDto

    }

    fun getFullTestByIdForUser(testId: Long, userId: Long): TestDto {
        val test = dslContext.selectFrom(TEST)
                .where(TEST.ID.eq(testId).and(TEST.DELETED.eq(false)))
                .fetchOneInto(Test::class.java)

        val testDto = mapTestToDto(test)

        fillInTestWithUserQuestions(testDto, userId)

        return testDto
    }

    private fun transformInputFields(testDto: TestDto) {
        testDto.questions.forEach { question ->
            if (question.type == QuestionType.CUSTOM_INPUT) {
                val document = Jsoup.parse(question.question)
                document.select(CSS_SELECTOR_CUSTOM_INPUTS).forEach { customInput ->
                    viewTransformers.find { customInput.tagName() == it.tagName }?.transform(customInput)
                }
                question.question = document.body().html()
            }
        }
    }

    fun invalidateTest(testDto: TestDto,
                       userId: Long) {
        val testId = testDto.id!!

        if (!testDto.isRetryable) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to invalidate this test")
        }

        dslContext.deleteFrom(COMPLETED_TEST)
                .where(COMPLETED_TEST.TEST_ID.eq(testId).and(COMPLETED_TEST.USER_ID.eq(userId)))
                .execute()

        val variants = questionService.getUserVersionQuestions(testId, userId)
                .map { it.variants }
                .reduce({ list1, list2 ->
                    val mutableList = list1.toMutableList()
                    mutableList.addAll(list2)
                    mutableList
                })

        dslContext.deleteFrom(CHOSEN_VARIANT)
                .where(CHOSEN_VARIANT.VARIANT_ID.`in`(variants.map { it.id }).and(CHOSEN_VARIANT.USER_ID.eq(userId)))
                .execute()

        variants.forEach { variant ->
            if (variant.isTicked && variant.inputType == "input") {
                dslContext.deleteFrom(VARIANT).where(VARIANT.ID.eq(variant.id)).execute()
            }
        } // ЭТО НЕ ДОЛЖНО РАБОТАТЬ!!!!
    }
}
