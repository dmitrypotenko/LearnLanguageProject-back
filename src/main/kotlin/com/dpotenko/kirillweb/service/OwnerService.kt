package com.dpotenko.kirillweb.service

import com.dpotenko.kirillweb.Tables
import com.dpotenko.kirillweb.Tables.COURSE
import com.dpotenko.kirillweb.Tables.COURSE_ACCESS
import com.dpotenko.kirillweb.Tables.TEST
import com.dpotenko.kirillweb.domain.UserPrincipal
import com.dpotenko.kirillweb.dto.CourseAccessLevel
import com.dpotenko.kirillweb.dto.CourseDto
import com.dpotenko.kirillweb.dto.CourseType
import com.dpotenko.kirillweb.dto.TestDto
import com.dpotenko.kirillweb.dto.UserAccessVO
import com.dpotenko.kirillweb.tables.pojos.Course
import com.dpotenko.kirillweb.tables.pojos.CourseAccess
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.authentication.session.SessionAuthenticationException
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class OwnerService(val dslContext: DSLContext,
                   val userService: UserService) {

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

    fun checkIsAllowedToEdit(test: TestDto?,
                             userPrincipal: UserPrincipal?) {
        if (isSuperAdmin(userPrincipal)) {
            return
        }

        if (test?.id == null || test.id == 0L) {
            return
        }

        val creators = findCreatorsForTest(test.id!!)

        val creatorsIds = creators.map { it.userId }

        if (!creatorsIds.contains(userPrincipal?.id)) {
            throw AccessDeniedException("You are not included in the list of creators of this course.")
        }
    }

    private fun findCreatorsForTest(testId: Long): List<CourseAccess> {
        return dslContext.select(*COURSE_ACCESS.fields()).from(COURSE_ACCESS.join(TEST).on(COURSE_ACCESS.COURSE_ID.eq(TEST.COURSE_ID)))
                .where(TEST.ID.eq(testId).and(COURSE_ACCESS.ACCESS_LEVEL.eq(CourseAccessLevel.OWNER)))
                .fetchInto(CourseAccess::class.java)
    }

    fun isSuperAdmin(userPrincipal: UserPrincipal?) =
            userPrincipal?.authorities?.map { it?.authority }?.contains("ROLE_SUPER_ADMIN") ?: false

    fun findCreators(courseId: Long): List<CourseAccess> {
        return dslContext.selectFrom(COURSE_ACCESS)
                .where(COURSE_ACCESS.COURSE_ID.eq(courseId).and(COURSE_ACCESS.ACCESS_LEVEL.eq(CourseAccessLevel.OWNER)))
                .fetchInto(CourseAccess::class.java)
    }

    fun saveCreator(userPrincipal: UserPrincipal?,
                    courseDto: CourseDto) {
        val creator = dslContext.selectFrom(COURSE_ACCESS)
                .where(COURSE_ACCESS.USER_ID.eq(userPrincipal?.id).and(COURSE_ACCESS.COURSE_ID.eq(courseDto.id)))
                .fetchOptionalInto(CourseAccess::class.java)
                .orElse(null)

        if (creator == null) {
            val newRecord = dslContext.newRecord(COURSE_ACCESS, CourseAccess(null, userPrincipal?.id, courseDto.id, CourseAccessLevel.OWNER))
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

    fun checkIsAllowedToEditComment(commentId: Long?,
                                    userPrincipal: UserPrincipal?) {
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

    fun checkAllowed(test: TestDto,
                     userPrincipal: UserPrincipal?) {
        val course = dslContext.select(*COURSE.fields())
                .from(fromSupplier.invoke(userPrincipal?.id).join(TEST).on(TEST.ID.eq(test.id!!).and(TEST.COURSE_ID.eq(COURSE.ID))))
                .where(COURSE.DELETED.eq(false).and(DSL.or(startCondition, DSL.condition(isSuperAdmin(userPrincipal)))))
                .fetchOneInto(Course::class.java)

        if (course == null) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "User ${userPrincipal?.id} don't have permissions to view test ${test.id}")
        }
    }

    fun checkAllowed(courseDto: CourseDto,
                     userPrincipal: UserPrincipal?,
                     key: String? = null) {
        if (userPrincipal?.id == null && courseDto.type == CourseType.PRIVATE) {
            throw SessionAuthenticationException("You must login to view this course.")
        }

        val course = dslContext.select(*COURSE.fields())
                .from(fromSupplier.invoke(userPrincipal?.id))
                .where(COURSE.DELETED.eq(false).and(COURSE.ID.eq(courseDto.id)).and(DSL.or(startCondition, DSL.condition(isSuperAdmin(userPrincipal)))))
                .fetchOneInto(Course::class.java)

        if (course == null) {
            if (key != null && courseDto.key == key && courseDto.type == CourseType.PRIVATE) {
                userService.updateCourseAccesses(courseDto.id!!, listOf(UserAccessVO(CourseAccessLevel.STUDENT, userPrincipal?.id!!)))
            } else {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "User ${userPrincipal?.id} don't have permissions to view course ${courseDto.id}")
            }
        }
    }

    fun saveKey(key: String,
                id: Long) {
        dslContext.update(COURSE)
                .set(COURSE.KEY, key)
                .where(COURSE.ID.eq(id))
                .execute()
    }

}
