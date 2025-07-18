package dev.icerock.education.practicetask.data.entities

import kotlinx.serialization.Serializable

@Serializable
data class RepoReadme(
    val content: String,
)
