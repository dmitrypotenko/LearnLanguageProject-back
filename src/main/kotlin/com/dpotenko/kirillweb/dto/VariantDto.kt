package com.dpotenko.kirillweb.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class VariantDto(
        val variant: String,
        val isRight: Boolean,
        val isWrong: Boolean,
        val isTicked: Boolean,
        var id: Long?
)