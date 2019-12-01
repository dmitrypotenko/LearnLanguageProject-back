package com.dpotenko.kirillweb.dto

data class VariantDto(
        val variant: String,
        val isRight: Boolean,
        val isWrong: Boolean,
        val isTicked: Boolean,
        var id: Long?
)