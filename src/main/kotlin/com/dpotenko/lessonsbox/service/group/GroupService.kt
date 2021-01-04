package com.dpotenko.lessonsbox.service.group

import com.dpotenko.lessonsbox.Tables.COMPLETED_TEST
import com.dpotenko.lessonsbox.Tables.GROUP
import com.dpotenko.lessonsbox.Tables.GROUP_COURSE
import com.dpotenko.lessonsbox.Tables.GROUP_USER
import com.dpotenko.lessonsbox.Tables.TEST
import com.dpotenko.lessonsbox.Tables.USER
import com.dpotenko.lessonsbox.domain.UserPrincipal
import com.dpotenko.lessonsbox.dto.AccessLevel
import com.dpotenko.lessonsbox.dto.UserDto
import com.dpotenko.lessonsbox.dto.group.GroupDto
import com.dpotenko.lessonsbox.tables.pojos.Group
import com.dpotenko.lessonsbox.tables.pojos.GroupCourse
import com.dpotenko.lessonsbox.tables.pojos.User
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.util.UUID


@Component
class GroupService(val dslContext: DSLContext,
                   val groupOwnerService: GroupOwnerService) {

    fun saveGroup(groupDto: GroupDto): Long {
        val group = Group(groupDto.name, groupDto.id, false, null)
        val groupRecord = dslContext.newRecord(GROUP, group)

        if (groupDto.id == null) {
            groupRecord.insert()
        } else {
            groupRecord.update()
        }
        return groupRecord.id
    }

    fun generateInviteKey(groupId: Long): String {
        val key = UUID.randomUUID().toString()

        dslContext.update(GROUP)
                .set(GROUP.KEY, key)
                .where(GROUP.ID.eq(groupId))
                .execute()

        return key
    }

    fun assignCoursesToGroup(courseIds: List<Long>,
                             groupId: Long) {
        val assignments = courseIds.map { courseId -> GroupCourse(null, groupId, courseId) }
                .map { groupCourse -> dslContext.newRecord(GROUP_COURSE, groupCourse) }

        dslContext.batchInsert(assignments).execute()
    }

    fun getGroups(userPrincipal: UserPrincipal?): List<GroupDto> {
        val groups = dslContext.selectDistinct(*GROUP.fields())
                .from(GROUP.leftJoin(GROUP_USER).on(GROUP.ID.eq(GROUP_USER.GROUP_ID).and(GROUP_USER.ACCESS_LEVEL.eq(AccessLevel.OWNER)
                        .or(GROUP_USER.ID.isNull()))))
                .where(GROUP.DELETED.isFalse()).and(GROUP_USER.USER_ID.eq(userPrincipal?.id).or(DSL.condition(groupOwnerService.isSuperAdmin(userPrincipal))))
                .fetchInto(Group::class.java)


        return groups.map { mapToDto(it) }
    }

    private fun mapToDto(it: Group) = GroupDto(it.id, it.name, it.key)

    fun deleteCoursesFromGroup(courseIds: List<Long>,
                               groupId: Long) {
        dslContext.deleteFrom(GROUP_COURSE)
                .where(GROUP_COURSE.COURSE_ID.`in`(*courseIds.toTypedArray()))
                .execute()
    }


    fun getGroup(groupId: Long,
                 userPrincipal: UserPrincipal?,
                 key: String?): GroupDto {
        val group = dslContext.select(*GROUP.fields())
                .from(GROUP)
                .where(GROUP.ID.eq(groupId).and(GROUP.DELETED.isFalse()))
                .fetchOneInto(Group::class.java)

        if (group == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "This group does not exist")
        }

        val dto = mapToDto(group)

        groupOwnerService.checkAllowedToViewGroup(dto, userPrincipal, key)

        return dto
    }

    fun getGroupsForTest(testId: Long,
                         userPrincipal: UserPrincipal?): List<GroupDto> {
        val fields = GROUP.fields().filter { it != GROUP.KEY }.toTypedArray()
        val groups = dslContext.select(*fields)
                .from(GROUP.leftJoin(GROUP_USER).on(GROUP.ID.eq(GROUP_USER.GROUP_ID).and(GROUP_USER.ACCESS_LEVEL.eq(AccessLevel.OWNER)
                        .or(GROUP_USER.ID.isNull()))).join(GROUP_COURSE).on(GROUP_COURSE.GROUP_ID.eq(GROUP.ID))
                        .join(TEST).on(TEST.COURSE_ID.eq(GROUP_COURSE.COURSE_ID)))
                .where(GROUP.DELETED.isFalse())
                .and(GROUP_USER.USER_ID.eq(userPrincipal?.id).or(DSL.condition(groupOwnerService.isSuperAdmin(userPrincipal))))
                .and(TEST.ID.eq(testId))
                .fetchInto(Group::class.java)


        val groupsDto = groups.map { mapToDto(it) }
        groupsDto.forEach { group -> group.users = findCompletedTestUsersForGroup(group.id!!, testId) }

        return groupsDto
    }

    private fun findCompletedTestUsersForGroup(groupId: Long,
                                               testId: Long): List<UserDto> {
        return dslContext.select(*USER.fields())
                .from(COMPLETED_TEST.join(USER).on(USER.ID.eq(COMPLETED_TEST.USER_ID))
                        .join(GROUP_USER).on(GROUP_USER.USER_ID.eq(USER.ID)))
                .where(COMPLETED_TEST.TEST_ID.eq(testId))
                .and(GROUP_USER.GROUP_ID.eq(groupId))
                .fetchInto(User::class.java)
                .sortedBy { it.name }
                .map {
                    mapUser(it)
                }
    }

    private fun mapUser(it: User): UserDto {
        return UserDto(
                it.id,
                it.name,
                it.imageurl,
                it.role.split(",")
        )
    }

}
