package com.dpotenko.kirillweb.dto

data class UserDto(
        val id: Long?,
        val name: String?,
        val pictureUrl: String?,
        val roles: List<String>
)
