package com.dpotenko.kirillweb.service

import com.dpotenko.kirillweb.Tables
import com.dpotenko.kirillweb.domain.UserPrincipal
import com.dpotenko.kirillweb.dto.CourseDto
import com.dpotenko.kirillweb.tables.pojos.CourseCreator
import org.jooq.DSLContext
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service

@Service
class OwnerService(val dslContext: DSLContext) {

    fun checkIsAllowedToEdit(courseId: Long?,
                             userPrincipal: UserPrincipal?) {
        if (isSuperAdmin(userPrincipal)) {
            return
        }
        if (courseId == null || courseId == 0L) {
            return
        }
        val creators = findCreators(courseId)

        val creatorsIds = creators.map { it.userId }

        if (!creatorsIds.contains(userPrincipal?.id)) {
            throw AccessDeniedException("You are not included in the list of creators of this course.")
        }
    }

    private fun isSuperAdmin(userPrincipal: UserPrincipal?) =
            userPrincipal?.authorities?.map { it?.authority }?.contains("ROLE_SUPER_ADMIN") ?: false

    fun findCreators(courseId: Long?): List<CourseCreator> {
        return dslContext.selectFrom(Tables.COURSE_CREATOR)
                .where(Tables.COURSE_CREATOR.COURSE_ID.eq(courseId))
                .fetchInto(CourseCreator::class.java)
    }

    fun saveCreator(userPrincipal: UserPrincipal?,
                    courseDto: CourseDto) {
        val creator = dslContext.selectFrom(Tables.COURSE_CREATOR)
                .where(Tables.COURSE_CREATOR.USER_ID.eq(userPrincipal?.id).and(Tables.COURSE_CREATOR.COURSE_ID.eq(courseDto.id)))
                .fetchOptionalInto(CourseCreator::class.java)
                .orElse(null)

        if (creator == null) {
            val newRecord = dslContext.newRecord(Tables.COURSE_CREATOR, CourseCreator(null, userPrincipal?.id, courseDto.id))
            newRecord.insert()
        }
    }

    fun findCommentCreator(commentId: Long?): Long {
        return dslContext.select(Tables.COMMENT.USER_ID)
                .from(Tables.COMMENT)
                .where(Tables.COMMENT.ID.eq(commentId))
                .fetchOne()
                .value1()
    }

    fun checkIsAllowedToEditComment(commentId: Long?, userPrincipal: UserPrincipal?) {
        if (isSuperAdmin(userPrincipal)) {
            return
        }
        if (commentId == null) {
            return
        }

        if (findCommentCreator(commentId) != userPrincipal?.id) {
            throw AccessDeniedException("The comment $commentId does not belong to user ${userPrincipal?.id}")
        }
    }

}
