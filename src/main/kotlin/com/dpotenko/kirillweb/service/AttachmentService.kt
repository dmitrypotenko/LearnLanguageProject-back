package com.dpotenko.kirillweb.service

import com.dpotenko.kirillweb.Tables
import com.dpotenko.kirillweb.dto.AttachmentDto
import com.dpotenko.kirillweb.tables.pojos.Attachment
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class AttachmentService(val dslContext: DSLContext) {
    fun saveAttachment(dto: AttachmentDto,
                       lessonId: Long): Long {
        val record = dslContext.newRecord(Tables.ATTACHMENT, Attachment(dto.id, dto.attachmentLink, dto.attachmentTitle, lessonId))

        if (dto.id == null) {
            record.insert()
        } else {
            record.update()
        }

        return record.id
    }
}