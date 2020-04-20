package com.dpotenko.kirillweb.service

import com.dpotenko.kirillweb.Tables.CHOSEN_VARIANT
import com.dpotenko.kirillweb.Tables.VARIANT
import com.dpotenko.kirillweb.dto.QuestionType
import com.dpotenko.kirillweb.dto.VariantDto
import com.dpotenko.kirillweb.tables.pojos.ChosenVariant
import com.dpotenko.kirillweb.tables.pojos.Variant
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Component

@Component
class VariantService(val dslContext: DSLContext) {
    fun saveVariant(dto: VariantDto,
                    questionId: Long,
                    questionType: QuestionType): Long {
        val record = dslContext.newRecord(VARIANT, Variant(dto.id, dto.isTicked, dto.isWrong, dto.variant, dto.isRight, dto.explanation, questionId, false, dto.inputName, dto.inputType))
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
                .where(VARIANT.QUESTION_ID.eq(questionId).and(VARIANT.DELETED.eq(false)))
                .fetch()
                .forEach { variantRecord ->
                    if (variantRecord.inputType == "input" && variantRecord.ticked && variants.find {  it.inputName == variantRecord.inputName }!=null) {
                        println("Skip user chosen variant")
                    } else if (variants.find { it.id == variantRecord.id } == null) {
                        variantRecord.deleted = true
                        variantRecord.store()
                    }
                }
    }


    fun getVariantsByQuestionId(questionId: Long): MutableList<VariantDto> {
        return dslContext.select(VARIANT.fields().toList())
                .from(VARIANT.leftJoin(CHOSEN_VARIANT).on(VARIANT.ID.eq(CHOSEN_VARIANT.VARIANT_ID)))
                .where(DSL.or(VARIANT.QUESTION_ID.eq(questionId).and(VARIANT.DELETED.eq(false)).and(VARIANT.INPUT_TYPE.notEqual("input").or(VARIANT.INPUT_TYPE.isNull)),
                        VARIANT.QUESTION_ID.eq(questionId).and(VARIANT.DELETED.eq(false)).and(VARIANT.INPUT_TYPE.eq("input").and(CHOSEN_VARIANT.ID.isNull())))

                )
                .fetchInto(Variant::class.java)
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

    fun getChosenVariantsForQuestion(questionId: Long,
                                     userId: Long): List<Variant> {
        return dslContext.selectFrom(CHOSEN_VARIANT.join(VARIANT).on(CHOSEN_VARIANT.VARIANT_ID.eq(VARIANT.ID)))
                .where(VARIANT.DELETED.eq(false).and(VARIANT.QUESTION_ID.eq(questionId))
                        .and(CHOSEN_VARIANT.USER_ID.eq(userId)))
                .fetchInto(Variant::class.java)
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
