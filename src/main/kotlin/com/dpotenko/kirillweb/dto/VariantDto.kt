package com.dpotenko.kirillweb.dto

import com.fasterxml.jackson.annotation.JsonProperty


data class VariantDto(
        var variant: String,
        @param:JsonProperty("isRight")
        @get:JsonProperty("isRight")
        var isRight: Boolean,
        @get:JsonProperty("isWrong")
        var isWrong: Boolean,
        @get:JsonProperty("isTicked")
        var isTicked: Boolean,
        var explanation: String?,
        var id: Long?,
        var inputName: String?,
        var inputType: String?
)
