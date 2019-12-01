package com.dpotenko.kirillweb.service

import com.dpotenko.kirillweb.Tables
import com.dpotenko.kirillweb.dto.VariantDto
import com.dpotenko.kirillweb.tables.pojos.Variant
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class VariantService(val dslContext: DSLContext) {
    fun saveVariant(dto: VariantDto,
                    questionId: Long): Long {
        val record = dslContext.newRecord(Tables.VARIANT, Variant(dto.id, dto.isTicked, dto.isWrong, dto.isRight, dto.variant, null, questionId, false))
        if (dto.id == null) {
            record.insert()
        } else {
            record.update()
        }

        return record.id
    }

    fun merge(variants: List<VariantDto>,
              questionId: Long) {
        dslContext.selectFrom(Tables.VARIANT)
                .where(Tables.VARIANT.QUESTION_ID.eq(questionId).and(Tables.VARIANT.DELETED.eq(false)))
                .fetch()
                .forEach { variantRecord ->
                    if (variants.find { it.id == variantRecord.id } == null) {
                        variantRecord.deleted = true
                        variantRecord.store()
                    }
                }
    }


    fun getVariantsByQuestionId(questionId: Long): List<VariantDto> {
        return dslContext.selectFrom(Tables.VARIANT)
                .where(Tables.VARIANT.QUESTION_ID.eq(questionId).and(Tables.VARIANT.DELETED.eq(false)))
                .fetchInto(Variant::class.java)
                .map { mapVariantToDto(it) }
    }

    private fun mapVariantToDto(variant: Variant): VariantDto {
        return VariantDto(
                variant.variantText,
                variant.right,
                variant.wrong,
                variant.ticked,
                variant.id
        )
    }

}