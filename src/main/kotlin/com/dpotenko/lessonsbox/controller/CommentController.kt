package com.dpotenko.lessonsbox.controller

import com.dpotenko.lessonsbox.domain.UserPrincipal
import com.dpotenko.lessonsbox.dto.comment.CommentDto
import com.dpotenko.lessonsbox.service.OwnerService
import com.dpotenko.lessonsbox.service.comment.CommentService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/comments")
class CommentController(private val ownerService: OwnerService,
                        private val commentService: CommentService) {

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    fun submitComment(@Valid @RequestBody commentDto: CommentDto, @AuthenticationPrincipal userPrincipal: UserPrincipal?): ResponseEntity<CommentDto> {
        commentDto.id = commentService.save(commentDto, userPrincipal?.id ?: throw AccessDeniedException("Can't create comment without a user"))
        return ResponseEntity.ok(commentDto)
    }

    @GetMapping("/{threadId}")
    fun getAllComments(@PathVariable(name = "threadId") threadId: String, @AuthenticationPrincipal userPrincipal: UserPrincipal?): ResponseEntity<List<CommentDto>> {
        return ResponseEntity.ok(commentService.get(threadId, userPrincipal ))
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    fun deleteComment(@PathVariable("id") id: Long, @AuthenticationPrincipal userPrincipal: UserPrincipal?) {
        ownerService.checkIsAllowedToEditComment(id, userPrincipal)
        commentService.delete(id)
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    fun updateComment(@Valid @RequestBody commentDto: CommentDto, @AuthenticationPrincipal userPrincipal: UserPrincipal?) {
        ownerService.checkIsAllowedToEditComment(commentDto.id, userPrincipal)
        commentService.update(commentDto, userPrincipal?.id ?: throw AccessDeniedException("Can't update comment without a user"))
    }
}
