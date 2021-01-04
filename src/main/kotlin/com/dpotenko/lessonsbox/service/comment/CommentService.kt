package com.dpotenko.lessonsbox.service.comment

import com.dpotenko.lessonsbox.Tables
import com.dpotenko.lessonsbox.domain.UserPrincipal
import com.dpotenko.lessonsbox.dto.comment.CommentDto
import com.dpotenko.lessonsbox.service.UserService
import com.dpotenko.lessonsbox.tables.pojos.Comment
import org.jooq.DSLContext
import org.jooq.Field
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CommentService(private val dslContext: DSLContext,
                     private val userService: UserService) {
    fun save(comment: CommentDto,
             userId: Long): Long {
        val newRecord = dslContext.newRecord(Tables.COMMENT, Comment(null, comment.commentText, LocalDateTime.now(), LocalDateTime.now(), userId, comment.threadId, false))

        newRecord.insert()
        comment.editable = true
        comment.createDate = LocalDateTime.now()
        comment.updateDate = LocalDateTime.now()
        userService.getUserInfo(userId).let {
            comment.userPic = it.pictureUrl
            comment.userName = it.name
        }
        return newRecord.id
    }

    fun delete(id: Long) {
        dslContext.update(Tables.COMMENT)
                .set(Tables.COMMENT.DELETED, true)
                .where(Tables.COMMENT.ID.eq(id))
                .execute()

    }

    fun update(comment: CommentDto,
               userId: Long) {
        if (!dslContext.fetchExists(Tables.COMMENT, Tables.COMMENT.ID.eq(comment.id))) {
            throw IllegalArgumentException("Comment ${comment.id} doesn't exist")
        }

        val newRecord = dslContext.newRecord(Tables.COMMENT, Comment(comment.id, comment.commentText, comment.createDate, LocalDateTime.now(), userId, comment.threadId, false))

        newRecord.update()
    }

    fun get(threadId: String,
            userPrincipal: UserPrincipal?): List<CommentDto> {
        val comment = Tables.COMMENT.`as`("comment")
        val user = Tables.USER.`as`("user")

        val comments = dslContext.select(*arrayOf<Field<*>>(user.NAME, user.IMAGEURL, user.ROLE).plus(comment.fields()))
                .from(comment.join(user).on(comment.USER_ID.eq(user.ID)))
                .where(comment.DELETED.isFalse().and(comment.THREAD_ID.eq(threadId)))
                .fetch()

        return comments.map {
            CommentDto(
                    it.get("id", Long::class.java),
                    it.get("comment_text", String::class.java),
                    it.get("thread_id", String::class.java),
                    it.get("create_date", LocalDateTime::class.java),
                    it.get("update_date", LocalDateTime::class.java),
                    it.get("user_id", Long::class.java) == userPrincipal?.id || userPrincipal?.isSuperAdmin() ?: false,
                    it.get("name", String::class.java),
                    it.get("imageurl", String::class.java)
            )
        }.sortedByDescending { it.createDate }
    }

}
