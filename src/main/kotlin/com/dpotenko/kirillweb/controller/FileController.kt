package com.dpotenko.kirillweb.controller

import com.dpotenko.kirillweb.dto.UploadResponse
import com.dpotenko.kirillweb.dto.UploadResponseCk
import com.dpotenko.kirillweb.service.FileService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/files")
class FileController(val fileService: FileService) {

    @PostMapping("/")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun handleFileUpload(@RequestParam("file") file: MultipartFile): ResponseEntity<UploadResponse> {
        return ResponseEntity.ok(UploadResponse(fileService.uploadAnyFile(file)))
    }

    @PostMapping("/upload")
     @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun handleFileUpload1(@RequestParam("file") file: MultipartFile): ResponseEntity<UploadResponse> {
        return ResponseEntity.ok(UploadResponse(fileService.uploadImageFile(file)))
    }

}
