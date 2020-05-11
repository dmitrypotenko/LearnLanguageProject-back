package com.dpotenko.kirillweb.service

import com.dpotenko.kirillweb.Tables
import com.dpotenko.kirillweb.dto.UserDto
import com.dpotenko.kirillweb.tables.pojos.User
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class UserService(private val dslContext: DSLContext) {

    fun getUserInfo(userId: Long): UserDto {
       return dslContext.selectFrom(Tables.USER)
                .where(Tables.USER.ID.eq(userId))
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
}
