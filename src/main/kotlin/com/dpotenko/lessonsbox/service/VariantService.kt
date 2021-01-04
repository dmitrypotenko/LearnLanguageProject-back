package com.dpotenko.lessonsbox.service

import com.dpotenko.lessonsbox.Tables.CHOSEN_VARIANT
import com.dpotenko.lessonsbox.Tables.VARIANT
import com.dpotenko.lessonsbox.dto.QuestionType
import com.dpotenko.lessonsbox.dto.VariantDto
import com.dpotenko.lessonsbox.tables.pojos.ChosenVariant
import com.dpotenko.lessonsbox.tables.pojos.Variant
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.stereotype.Component

@Component
class VariantService(val dslContext: DSLContext) {
    fun saveVariant(dto: VariantDto,
                    questionId: Long,
                    questionType: QuestionType): Long {
        val record = dslContext.newRecord(VARIANT, Variant(dto.id, dto.isTicked, dto.isWrong, dto.isRight, dto.variant, dto.explanation, questionId, false, dto.inputName, dto.inputType))
        if (dto.id == null) {
            record.insert()
        } else {
            record.update()
        }

        return record.id
    }

    fun merge(variants: List<VariantDto>,
              questionId: Long) {
        dslContext.selectFrom(VARIANT)
                .where(VARIANT.QUESTION_ID.eq(questionId).and(VARIANT.DELETED.eq(false)).and(DSL.not(VARIANT.INPUT_TYPE.eq("input")
                        .and(VARIANT.TICKED.isTrue()).and(VARIANT.INPUT_NAME.`in`(variants.map { it.inputName })))))
                .fetch()
                .forEach { variantRecord ->
                    if (variants.find { it.id == variantRecord.id } == null) {
                        variantRecord.deleted = true
                        variantRecord.store()
                    }
                }
    }


    fun getVariantsByQuestionId(questionId: Long): MutableList<VariantDto> {
        return dslContext.selectDistinct(VARIANT.fields().toList())
                .from(VARIANT.leftJoin(CHOSEN_VARIANT).on(VARIANT.ID.eq(CHOSEN_VARIANT.VARIANT_ID)))
                .where(DSL.or(VARIANT.QUESTION_ID.eq(questionId).and(VARIANT.DELETED.eq(false)).and(VARIANT.INPUT_TYPE.notEqual("input").or(VARIANT.INPUT_TYPE.isNull())),
                        VARIANT.QUESTION_ID.eq(questionId).and(VARIANT.DELETED.eq(false)).and(VARIANT.INPUT_TYPE.eq("input").and(CHOSEN_VARIANT.ID.isNull())))

                )
                .fetchInto(Variant::class.java)
                .map { mapVariantToDto(it) }
                .toMutableList()
    }

    fun getUserVariantsByQuestionId(questionId: Long,
                                    userId: Long): MutableList<VariantDto> {
        return dslContext.fetch("select variant.id, cv.id is not null as ticked, variant.wrong, variant.\"right\", variant.variant_text, variant.explanation,\n" +
                "       variant.question_id,variant.deleted,variant.input_name, variant.input_type\n" +
                "from variant left join chosen_variant cv on variant.id = cv.variant_id  and cv.user_id=? where variant.deleted=false and variant.question_id=?\n" +
                "and (cv.user_id=? or cv.id is null)", userId, questionId, userId)
                .map { record: Record -> record.into(Variant::class.java) }
                .filterNot { variant -> variant.inputType == "input" && variant.ticked == false }
                .map { mapVariantToDto(it) }
                .toMutableList()
    }

    fun markAsChosenVariant(userId: Long,
                            variant: VariantDto) {
        val chosenVariant = ChosenVariant()
        chosenVariant.userId = userId
        chosenVariant.variantId = variant.id!!
        val newRecord = dslContext.newRecord(CHOSEN_VARIANT, chosenVariant)
        newRecord.insert()
    }

    private fun mapVariantToDto(variant: Variant): VariantDto {
        return VariantDto(
                variant.variantText,
                variant.right,
                variant.wrong,
                variant.ticked,
                variant.explanation,
                variant.id,
                variant.inputName,
                variant.inputType
        )
    }

}
