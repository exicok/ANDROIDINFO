package com.example.info.battery

import kotlinx.serialization.Serializable

@Serializable
data class CustomEntry(
    val title: String,
    val path: String,
    val unit: String = "",
    val scale: Int = 0
)
