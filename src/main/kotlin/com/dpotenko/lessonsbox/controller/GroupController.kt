package com.dpotenko.lessonsbox.controller

import com.dpotenko.lessonsbox.domain.UserPrincipal
import com.dpotenko.lessonsbox.dto.group.GroupDto
import com.dpotenko.lessonsbox.service.group.GroupOwnerService
import com.dpotenko.lessonsbox.service.group.GroupService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/groups")
class GroupController(val groupService: GroupService,
                      val groupOwnerService: GroupOwnerService) {

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun submitGroup(@RequestBody groupDto: GroupDto, @AuthenticationPrincipal userPrincipal: UserPrincipal?): ResponseEntity<GroupDto> {
        groupOwnerService.checkIsAllowedToEditGroup(groupDto.id, userPrincipal)
        groupDto.id = groupService.saveGroup(groupDto)
        groupOwnerService.saveCreator(userPrincipal, groupDto.id!!)
        return ResponseEntity.ok(groupDto)
    }

    @PostMapping("/{id}/keys")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun saveKey(@PathVariable("id") id: Long, @AuthenticationPrincipal userPrincipal: UserPrincipal?): String {
        groupOwnerService.checkIsAllowedToEditGroup(id, userPrincipal)

        return groupService.generateInviteKey(id)
    }


    @PostMapping("/{groupId}/courses")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun assignCourses(@RequestBody editCoursesAssignmentRequest: EditCoursesAssignmentRequest,
                      @PathVariable("groupId") groupId: Long,
                      @AuthenticationPrincipal userPrincipal: UserPrincipal?) {
        groupOwnerService.checkIsAllowedToEditGroup(groupId, userPrincipal)

        groupService.assignCoursesToGroup(editCoursesAssignmentRequest.courseIds, groupId)
    }

    @DeleteMapping("/{groupId}/courses")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun deleteCourses(@RequestParam courseIds: List<Long>,
                      @PathVariable("groupId") groupId: Long,
                      @AuthenticationPrincipal userPrincipal: UserPrincipal?) {
        groupOwnerService.checkIsAllowedToEditGroup(groupId, userPrincipal)

        groupService.deleteCoursesFromGroup(courseIds, groupId)
    }

    @GetMapping
    fun getGroups(@AuthenticationPrincipal userPrincipal: UserPrincipal?): ResponseEntity<List<GroupDto>> {
        return ResponseEntity.ok(groupService.getGroups(userPrincipal))
    }

    @GetMapping("/{groupId}")
    fun getGroup(@AuthenticationPrincipal userPrincipal: UserPrincipal?,
                 @RequestParam("key", required = false) key: String?,
                 @PathVariable("groupId") groupId: Long): ResponseEntity<GroupDto> {
        return ResponseEntity.ok(groupService.getGroup(groupId, userPrincipal, key))
    }

    @GetMapping("/tests/{testId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun getGroupsForTest(@PathVariable("testId") testId: Long,
                         @AuthenticationPrincipal userPrincipal: UserPrincipal?): List<GroupDto> {
        return groupService.
                getGroupsForTest(testId, userPrincipal)
    }
}

data class EditCoursesAssignmentRequest(val courseIds: List<Long>)
