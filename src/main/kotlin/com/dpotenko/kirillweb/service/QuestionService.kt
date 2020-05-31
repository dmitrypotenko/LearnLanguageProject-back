package com.dpotenko.kirillweb.service

import com.dpotenko.kirillweb.Tables
import com.dpotenko.kirillweb.dto.QuestionDto
import com.dpotenko.kirillweb.tables.pojos.Question
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class QuestionService(val dslContext: DSLContext,
                      val variantService: VariantService) {
    fun saveQuestion(dto: QuestionDto,
                     testId: Long): Long {
        val record = dslContext.newRecord(Tables.QUESTION, Question(dto.question, dto.type, dto.id, testId, false))
        if (dto.id == null) {
            record.insert()
        } else {
            record.update()
        }

        return record.id
    }

    fun merge(questions: List<QuestionDto>,
              testId: Long) {
        dslContext.selectFrom(Tables.QUESTION)
                .where(Tables.QUESTION.TEST_ID.eq(testId).and(Tables.QUESTION.DELETED.eq(false)))
                .fetch()
                .forEach { questionRecord ->
                    val foundQuestion = questions.find { it.id == questionRecord.id }
                    if (foundQuestion == null) {
                        questionRecord.deleted = true
                        questionRecord.store()
                    } else {
                        variantService.merge(foundQuestion.variants, foundQuestion.id!!)
                    }
                }
    }


    fun getQuestionsByTestId(testId: Long): List<QuestionDto> {
        val questions = dslContext.selectFrom(Tables.QUESTION)
                .where(Tables.QUESTION.TEST_ID.eq(testId).and(Tables.QUESTION.DELETED.eq(false)))
                .fetchInto(Question::class.java)

        val dtos = questions.map { mapQuestionToDto(it) }

        dtos.forEach { questionDto -> questionDto.variants = variantService.getVariantsByQuestionId(questionDto.id!!) }

        return dtos
    }

    fun getUserVersionQuestions(testId: Long, userId:Long): List<QuestionDto> {
        val questions = dslContext.selectFrom(Tables.QUESTION)
                .where(Tables.QUESTION.TEST_ID.eq(testId).and(Tables.QUESTION.DELETED.eq(false)))
                .fetchInto(Question::class.java)

        val dtos = questions.map { mapQuestionToDto(it) }

        dtos.forEach { questionDto -> questionDto.variants = variantService.getUserVariantsByQuestionId(questionDto.id!!, userId) }

        return dtos
    }


    private fun mapQuestionToDto(question: Question): QuestionDto {
        return QuestionDto(
                question.questionText,
                mutableListOf(),
                question.type,
                question.id
        )
    }
}
