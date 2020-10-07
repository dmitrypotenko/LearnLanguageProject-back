package com.dpotenko.kirillweb.service

import com.dpotenko.kirillweb.Keys
import com.dpotenko.kirillweb.Tables.COMPLETED_TEST
import com.dpotenko.kirillweb.Tables.COURSE_ACCESS
import com.dpotenko.kirillweb.Tables.GROUP_USER
import com.dpotenko.kirillweb.Tables.USER
import com.dpotenko.kirillweb.converter.AccessLevelConverter
import com.dpotenko.kirillweb.dto.AccessLevel
import com.dpotenko.kirillweb.dto.UserAccess
import com.dpotenko.kirillweb.dto.UserAccessVO
import com.dpotenko.kirillweb.dto.UserDto
import com.dpotenko.kirillweb.tables.pojos.User
import org.jooq.DSLContext
import org.jooq.Record
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
                .map { record -> mapToUserAccess(record) }
    }

    fun getGroupAccesses(groupId: Long): List<UserAccess> {
        return dslContext.select(*USER.fields() + GROUP_USER.ACCESS_LEVEL)
                .from(USER.leftJoin(GROUP_USER).on(GROUP_USER.USER_ID.eq(USER.ID).and(GROUP_USER.GROUP_ID.eq(groupId))))
                .where(GROUP_USER.GROUP_ID.eq(groupId).or(GROUP_USER.GROUP_ID.isNull()))
                .fetch()
                .map { record -> mapToUserAccess(record) }
    }

    fun getGroupStudents(groupId: Long): List<UserAccess> {
        return dslContext.select(*USER.fields() + GROUP_USER.ACCESS_LEVEL)
                .from(USER.leftJoin(GROUP_USER).on(GROUP_USER.USER_ID.eq(USER.ID).and(GROUP_USER.GROUP_ID.eq(groupId))))
                .where(GROUP_USER.GROUP_ID.eq(groupId).and(GROUP_USER.ACCESS_LEVEL.eq(AccessLevel.STUDENT)))
                .fetch()
                .map { record -> mapToUserAccess(record) }
    }

    private fun mapToUserAccess(record: Record): UserAccess {
        return UserAccess(
                record.get("access_level", AccessLevelConverter()),
                UserDto(record.get("id", Long::class.java),
                        record.get("name", String::class.java),
                        record.get("imageurl", String::class.java),
                        record.get("role", String()::class.java).split(","))
        )
    }

    fun updateCourseAccesses(courseId: Long,
                             accesses: List<UserAccessVO>) {
        accesses.forEach { access ->
            dslContext.insertInto(COURSE_ACCESS, COURSE_ACCESS.COURSE_ID, COURSE_ACCESS.ACCESS_LEVEL, COURSE_ACCESS.USER_ID)
                    .values(courseId, access.accessLevel, access.userId)
                    .onConflictOnConstraint(Keys.COURSE_ACCESS_PK)
                    .doUpdate()
                    .set(COURSE_ACCESS.COURSE_ID, courseId)
                    .set(COURSE_ACCESS.ACCESS_LEVEL, access.accessLevel)
                    .set(COURSE_ACCESS.USER_ID, access.userId)
                    .execute()
        }
    }

    fun updateGroupAccesses(groupId: Long,
                            accesses: List<UserAccessVO>) {
        accesses.forEach { access ->
            dslContext.insertInto(GROUP_USER, GROUP_USER.GROUP_ID, GROUP_USER.ACCESS_LEVEL, GROUP_USER.USER_ID)
                    .values(groupId, access.accessLevel, access.userId)
                    .onConflictOnConstraint(Keys.GROUP_USER_PK_2)
                    .doUpdate()
                    .set(GROUP_USER.GROUP_ID, groupId)
                    .set(GROUP_USER.ACCESS_LEVEL, access.accessLevel)
                    .set(GROUP_USER.USER_ID, access.userId)
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
