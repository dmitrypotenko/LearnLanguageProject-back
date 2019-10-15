package com.dpotenko.kirillweb.dto

data class VariantDto(
        val variantText: String,
        val isRight: Boolean,
        val isWrong: Boolean,
        val isTicked: Boolean,
        val id: Long
)