package com.dpotenko.lessonsbox.dto

data class UserAccess(val accessLevel: AccessLevel,
                      val userDto: UserDto) {

}


data class UserAccessVO(val accessLevel: AccessLevel,
                        val userId: Long)
