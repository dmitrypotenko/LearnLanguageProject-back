package com.dpotenko.kirillweb.service

import com.dpotenko.kirillweb.Keys
import com.dpotenko.kirillweb.Tables.COMPLETED_TEST
import com.dpotenko.kirillweb.Tables.COURSE_ACCESS
import com.dpotenko.kirillweb.Tables.USER
import com.dpotenko.kirillweb.converter.CourseAccessLevelConverter
import com.dpotenko.kirillweb.dto.UserAccess
import com.dpotenko.kirillweb.dto.UserAccessVO
import com.dpotenko.kirillweb.dto.UserDto
import com.dpotenko.kirillweb.tables.pojos.User
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class UserService(private val dslContext: DSLContext) {

    fun getUserInfo(userId: Long): UserDto {
        return dslContext.selectFrom(USER)
                .where(USER.ID.eq(userId))
                .fetchOneInto(User::class.java)
                .let {
                    UserDto(
                            userId,
                            it.name,
                            it.imageurl,
                            it.role.split(",")
                    )
                }
    }

    fun getSubmittedTestUsers(testId: Long): List<UserDto> {
        return dslContext.select(*USER.fields())
                .from(COMPLETED_TEST.join(USER).on(USER.ID.eq(COMPLETED_TEST.USER_ID)))
                .where(COMPLETED_TEST.TEST_ID.eq(testId))
                .fetchInto(User::class.java)
                .sortedBy { it.name }
                .map {
                    map(it)
                }
    }

    fun getAllUsers(): List<UserDto> {
        return dslContext.select(*USER.fields())
                .from(USER)
                .fetchInto(User::class.java)
                .map {
                    map(it)
                }
    }

    fun getUserAccesses(courseId: Long): List<UserAccess> {
        return dslContext.select(*USER.fields() + COURSE_ACCESS.ACCESS_LEVEL)
                .from(USER.leftJoin(COURSE_ACCESS).on(COURSE_ACCESS.USER_ID.eq(USER.ID).and(COURSE_ACCESS.COURSE_ID.eq(courseId))))
                .where(COURSE_ACCESS.COURSE_ID.eq(courseId).or(COURSE_ACCESS.COURSE_ID.isNull()))
                .fetch()
                .map { record ->
                    UserAccess(
                            record.get("access_level", CourseAccessLevelConverter()),
                            UserDto(record.get("id", Long::class.java),
                                    record.get("name", String::class.java),
                                    record.get("imageurl", String::class.java),
                                    record.get("role", String()::class.java).split(","))
                    )
                }
    }

    fun updateCourseAccesses(courseId: Long,
                             accesses: List<UserAccessVO>) {
        accesses.forEach { access ->
            dslContext.insertInto(COURSE_ACCESS, COURSE_ACCESS.COURSE_ID, COURSE_ACCESS.ACCESS_LEVEL, COURSE_ACCESS.USER_ID)
                    .values(courseId, access.courseAccessLevel, access.userId)
                    .onConflictOnConstraint(Keys.COURSE_ACCESS_PK)
                    .doUpdate()
                    .set(COURSE_ACCESS.COURSE_ID, courseId)
                    .set(COURSE_ACCESS.ACCESS_LEVEL, access.courseAccessLevel)
                    .set(COURSE_ACCESS.USER_ID, access.userId)
                    .execute()
        }
    }

    private fun map(it: User): UserDto {
        return UserDto(
                it.id,
                it.name,
                it.imageurl,
                it.role.split(",")
        )
    }
}
