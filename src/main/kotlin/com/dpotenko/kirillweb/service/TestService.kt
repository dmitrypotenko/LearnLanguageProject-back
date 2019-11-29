package com.dpotenko.kirillweb.service

import com.dpotenko.kirillweb.Tables
import com.dpotenko.kirillweb.dto.TestDto
import com.dpotenko.kirillweb.tables.pojos.Test
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class TestService(val dslContext: DSLContext,
                  val questionService: QuestionService) {

    fun saveTest(dto: TestDto,
                 courseId: Long): Long {
        val record = dslContext.newRecord(Tables.TEST, Test(dto.id, dto.name, dto.order.toInt(), courseId, false))
        if (dto.id == null) {
            record.insert()
        } else {
            record.update()
        }

        return record.id
    }

    fun merge(tests: List<TestDto>,
              courseId: Long) {
        dslContext.selectFrom(Tables.TEST)
                .where(Tables.TEST.COURSE_ID.eq(courseId))
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
}