package com.dpotenko.kirillweb.service

import com.dpotenko.kirillweb.Tables.CHOSEN_VARIANT
import com.dpotenko.kirillweb.Tables.COMPLETED_TEST
import com.dpotenko.kirillweb.Tables.TEST
import com.dpotenko.kirillweb.Tables.VARIANT
import com.dpotenko.kirillweb.dto.QuestionStatus
import com.dpotenko.kirillweb.dto.TestDto
import com.dpotenko.kirillweb.dto.VariantDto
import com.dpotenko.kirillweb.service.question.QuestionChecker
import com.dpotenko.kirillweb.tables.pojos.CompletedTest
import com.dpotenko.kirillweb.tables.pojos.Test
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class TestService(val dslContext: DSLContext,
                  val questionService: QuestionService,
                  val variantService: VariantService,
                  val questionChekers: List<QuestionChecker>) {

    fun saveTest(dto: TestDto,
                 courseId: Long): Long {
        val record = dslContext.newRecord(TEST, Test(dto.id, dto.name, dto.order.toInt(), courseId, false))
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

    fun getTestsByCourseId(courseId: Long): List<TestDto> {
        val tests = dslContext.selectFrom(TEST)
                .where(TEST.COURSE_ID.eq(courseId).and(TEST.DELETED.eq(false)))
                .fetchInto(Test::class.java)

        val testsDto = tests.map { mapTestToDto(it) }

        testsDto.forEach { testDto -> testDto.questions = questionService.getQuestionsByTestId(testDto.id!!) }

        return testsDto
    }

    fun checkTest(userTestDto: TestDto): TestDto {
        val test = dslContext.selectFrom(TEST)
                .where(TEST.ID.eq(userTestDto.id).and(TEST.DELETED.eq(false)))
                .fetchOneInto(Test::class.java)

        val testDto = mapTestToDto(test)
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

    fun markAsCompleted(testDto: TestDto,
                        userId: Long) {
        val existingRecord = getCompletedTest(userId, testDto.id!!)
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
                                variantDto.id = variantService.saveVariant(variantDto, questionId = question.id!!, questionType = question.type)
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
                test.orderNumber.toLong(),
                test.id
        )
    }

    fun invalidateTest(testId: Long,
                       userId: Long) {
        dslContext.deleteFrom(COMPLETED_TEST)
                .where(COMPLETED_TEST.TEST_ID.eq(testId).and(COMPLETED_TEST.USER_ID.eq(userId)))
                .execute()

        val variants = questionService.getQuestionsByTestId(testId)
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
        }


    }
}
