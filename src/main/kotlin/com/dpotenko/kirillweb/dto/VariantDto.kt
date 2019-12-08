package com.dpotenko.kirillweb.dto

import com.fasterxml.jackson.annotation.JsonProperty


data class VariantDto(
        val variant: String,
        @param:JsonProperty("isRight")
        @get:JsonProperty("isRight")
        val isRight: Boolean,
        @get:JsonProperty("isWrong")
        val isWrong: Boolean,
        @get:JsonProperty("isTicked")
        val isTicked: Boolean,
        var id: Long?
)