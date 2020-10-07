package com.dpotenko.kirillweb.dto.group

import com.dpotenko.kirillweb.dto.UserDto

data class GroupDto(var id: Long?,
                    val name: String,
                    val key: String?) {
    var users: List<UserDto> = listOf()
}
