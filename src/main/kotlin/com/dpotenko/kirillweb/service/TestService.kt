package com.dpotenko.kirillweb.service

import com.dpotenko.kirillweb.Tables
import com.dpotenko.kirillweb.dto.TestDto
import com.dpotenko.kirillweb.tables.pojos.Test
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class TestService(val dslContext: DSLContext) {

    fun saveTest(dto: TestDto,
                 courseId: Long): Long {
        val record = dslContext.newRecord(Tables.TEST, Test(dto.id, dto.name, dto.order.toInt(), courseId))
        if (dto.id == null) {
            record.insert()
        } else {
            record.update()
        }

        return record.id
    }
}