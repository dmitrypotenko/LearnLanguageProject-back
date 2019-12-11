package com.dpotenko.kirillweb.dto

import com.fasterxml.jackson.annotation.JsonProperty


data class VariantDto(
        val variant: String,
        @param:JsonProperty("isRight")
        @get:JsonProperty("isRight")
        var isRight: Boolean,
        @get:JsonProperty("isWrong")
        var isWrong: Boolean,
        @get:JsonProperty("isTicked")
        var isTicked: Boolean,
        var id: Long?
)