package dev.icerock.education.practicetask.data.repository

import retrofit2.Response
import dev.icerock.education.practicetask.data.api.GithubApi
import dev.icerock.education.practicetask.data.api.KeyValueStorage
import dev.icerock.education.practicetask.data.entities.Repo
import dev.icerock.education.practicetask.data.entities.RepoDetails
import dev.icerock.education.practicetask.data.entities.RepoReadme
import dev.icerock.education.practicetask.data.entities.UserInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val githubApi: GithubApi,
    private val keyValueStorage: KeyValueStorage,
) {
    val isAuthorized: Boolean
        get() = keyValueStorage.isAuthorized

    fun logOut() {
        keyValueStorage.isAuthorized = false
    }

    suspend fun getRepositories(): Response<List<Repo>> = githubApi.getRepositoriesList(
        authHeader = "Bearer ${keyValueStorage.authToken}",
        userName = keyValueStorage.userName!!
    )

    suspend fun getRepository(repoId: String): Response<RepoDetails> = githubApi
        .getRepositoryDetails(
            ownerName = keyValueStorage.userName!!,
            repoName = repoId,
            authHeader = "Bearer ${keyValueStorage.authToken}",
        )

    suspend fun getRepositoryReadme(
        ownerName: String,
        repositoryName: String,
        branchName: String,
    ): Response<RepoReadme> = githubApi.getRepositoryReadme(
        ownerName,
        repositoryName,
        branchName,
        authHeader = "Bearer ${keyValueStorage.authToken}"
    )


    suspend fun signIn(token: String): Response<UserInfo> {
        val res = githubApi.getUserInfo(
            authHeader = "Bearer $token"
        )
        if (res.isSuccessful) {
            keyValueStorage.authToken = token
            keyValueStorage.userName = res.body()?.login
            keyValueStorage.isAuthorized = true
        }
        return res
    }
}