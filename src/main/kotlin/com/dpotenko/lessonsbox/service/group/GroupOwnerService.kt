package com.dpotenko.lessonsbox.service.group

import com.dpotenko.lessonsbox.Tables.GROUP_USER
import com.dpotenko.lessonsbox.domain.UserPrincipal
import com.dpotenko.lessonsbox.dto.AccessLevel
import com.dpotenko.lessonsbox.dto.UserAccessVO
import com.dpotenko.lessonsbox.dto.group.GroupDto
import com.dpotenko.lessonsbox.service.OwnerService
import com.dpotenko.lessonsbox.service.UserService
import com.dpotenko.lessonsbox.tables.pojos.CourseAccess
import com.dpotenko.lessonsbox.tables.pojos.GroupUser
import org.jooq.DSLContext
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.authentication.session.SessionAuthenticationException
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class GroupOwnerService(dslContext: DSLContext,
                        userService: UserService) : OwnerService(dslContext, userService) {

    fun saveCreator(userPrincipal: UserPrincipal?,
                    groupId: Long) {
        val creator = dslContext.selectFrom(GROUP_USER)
                .where(GROUP_USER.USER_ID.eq(userPrincipal?.id).and(GROUP_USER.GROUP_ID.eq(groupId)))
                .fetchOptionalInto(CourseAccess::class.java)
                .orElse(null)

        if (creator == null) {
            val newRecord = dslContext.newRecord(GROUP_USER, GroupUser(null, userPrincipal?.id, groupId, AccessLevel.OWNER))
            newRecord.insert()
        }
    }

    fun checkIsAllowedToEditGroup(groupId: Long?,
                                  userPrincipal: UserPrincipal?) {
        if (isSuperAdmin(userPrincipal)) {
            return
        }
        if (groupId == null || groupId == 0L) {
            return
        }
        val creators = findCreatorsGroup(groupId)

        val creatorsIds = creators.map { it.userId }

        if (!creatorsIds.contains(userPrincipal?.id)) {
            throw AccessDeniedException("You are not included in the list of creators of this course.")
        }
    }

    fun findCreatorsGroup(groupId: Long): List<CourseAccess> {
        return dslContext.selectFrom(GROUP_USER)
                .where(GROUP_USER.GROUP_ID.eq(groupId).and(GROUP_USER.ACCESS_LEVEL.eq(AccessLevel.OWNER)))
                .fetchInto(CourseAccess::class.java)
    }

    fun checkAllowedToViewGroup(group: GroupDto,
                                userPrincipal: UserPrincipal?,
                                key: String? = null) {
        if (userPrincipal?.id == null) {
            throw SessionAuthenticationException("You must login to view this group.")
        }

        val groupId = group.id

        val groupUser: GroupUser? = dslContext.select(*GROUP_USER.fields())
                .from(GROUP_USER)
                .where(GROUP_USER.GROUP_ID.eq(groupId).and(GROUP_USER.USER_ID.eq(userPrincipal.id)))
                .fetchOneInto(GroupUser::class.java)

        if (groupUser == null || groupUser.accessLevel == AccessLevel.NONE) {
            if (key != null && group.key == key) {
                userService.updateGroupAccesses(groupId!!, listOf(UserAccessVO(AccessLevel.STUDENT, userPrincipal.id)))
            } else {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "User ${userPrincipal.id} don't have permissions to view group $groupId")
            }
        }
    }

}
