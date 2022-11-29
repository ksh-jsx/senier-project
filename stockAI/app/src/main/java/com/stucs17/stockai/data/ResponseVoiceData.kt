package com.stucs17.stockai.data
import kotlinx.serialization.Serializable

@Serializable
data class ResponseVoiceData (
    val command : Int,
    val view : Int,
    val value : Int,
    val stock: String,
    val price: Int,
)