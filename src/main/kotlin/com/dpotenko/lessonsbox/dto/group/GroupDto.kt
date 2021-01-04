package com.dpotenko.lessonsbox.dto.group

import com.dpotenko.lessonsbox.dto.UserDto

data class GroupDto(var id: Long?,
                    val name: String,
                    val key: String?) {
    var users: List<UserDto> = listOf()
}
