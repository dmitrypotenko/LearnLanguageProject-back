package com.dpotenko.lessonsbox.service

import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.Date

@Component
class FileService {
    @Autowired
    private lateinit var storage: Storage

    fun uploadImageFile(fileStream: MultipartFile): String {

        val fileName: String = Date().time.toString() + fileStream.originalFilename

        val blobInfo: BlobInfo = storage.createFrom(
                BlobInfo
                        .newBuilder("lessonsbox", fileName) // Modify access list to allow all users with link to read file
                        .setContentType("image/" + fileName.substringAfterLast("."))
                        .build(),
                fileStream.inputStream)

        // return the public download link
        return "https://storage.googleapis.com/${blobInfo.bucket}/${blobInfo.name}"
    }

    fun uploadAnyFile(fileStream: MultipartFile): String {

        val fileName: String = Date().time.toString() + fileStream.originalFilename

        val blobInfo: BlobInfo = storage.createFrom(
                BlobInfo
                        .newBuilder("lessonsbox", fileName) // Modify access list to allow all users with link to read file
                        .build(),
                fileStream.inputStream)

        // return the public download link
        return blobInfo.mediaLink
    }
}
