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
        val record = dslContext.newRecord(Tables.ATTACHMENT, Attachment(dto.id, dto.attachmentLink, dto.attachmentTitle, lessonId, false))

        if (dto.id == null) {
            record.insert()
        } else {
            record.update()
        }

        return record.id
    }

    fun merge(attachments: List<AttachmentDto>,
              lessonId: Long) {
        dslContext.selectFrom(Tables.ATTACHMENT)
                .where(Tables.ATTACHMENT.LESSON_ID.eq(lessonId))
                .fetch()
                .forEach { attachmentRecord ->
                    if (attachments.find { it.id == attachmentRecord.id } == null) {
                        attachmentRecord.deleted = true
                        attachmentRecord.store()
                    }
                }
    }
}