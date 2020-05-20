package com.dpotenko.kirillweb.dto

data class UserAccess(val courseAccessLevel: CourseAccessLevel,
                      val userDto: UserDto) {

}


data class UserAccessVO(val courseAccessLevel: CourseAccessLevel,
                        val userId: Long)
