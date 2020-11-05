package com.dpotenko.kirillweb.service

import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.storage.Acl
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileInputStream
import java.util.ArrayList
import java.util.Arrays
import java.util.Date
import javax.annotation.PostConstruct

@Component
class FileService {
    private lateinit var storage: Storage

    @Value("\${google.storage}")
    private lateinit var googleStorageKey: String

    @PostConstruct
    fun init() {
        if (googleStorageKey == "NONE") {
            storage = StorageOptions.getDefaultInstance().getService();
        } else {
            storage = StorageOptions.newBuilder().setCredentials(ServiceAccountCredentials.fromStream(
                    FileInputStream(File(googleStorageKey))
            )).build().service
        }

    }

    fun uploadImageFile(fileStream: MultipartFile): String {

        val fileName: String = Date().time.toString() + fileStream.originalFilename

        val blobInfo: BlobInfo = storage.create(
                BlobInfo
                        .newBuilder("dpotenko", fileName) // Modify access list to allow all users with link to read file
                        .setContentType("image/" + fileName.substringAfterLast("."))
                        .build(),
                fileStream.inputStream)
        // return the public download link
        // return the public download link
        return "https://storage.googleapis.com/${blobInfo.bucket}/${blobInfo.name}"
    }

    fun uploadAnyFile(fileStream: MultipartFile): String {

        val fileName: String = Date().time.toString() + fileStream.originalFilename

        val blobInfo: BlobInfo = storage.create(
                BlobInfo
                        .newBuilder("dpotenko", fileName) // Modify access list to allow all users with link to read file
                        .build(),
                fileStream.inputStream)
        // return the public download link
        // return the public download link
        return blobInfo.mediaLink
    }
}
