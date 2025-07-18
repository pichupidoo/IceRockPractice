package dev.icerock.education.practicetask.data.entities

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val login: String,
)
