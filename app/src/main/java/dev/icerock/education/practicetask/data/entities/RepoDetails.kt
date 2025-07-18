package dev.icerock.education.practicetask.data.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepoDetails(
    val name: String,
    val owner: UserInfo,
    @SerialName("default_branch") val defaultBranch: String,
    @SerialName("forks_count") val forksCount: Int,
    @SerialName("watchers_count") val watchersCount: Int,
    @SerialName("stargazers_count") val starsCount: Int,
    val license: License?,
    val description: String?,
    @SerialName("html_url") val url: String,
)

@Serializable
data class License(
    val name: String,
)