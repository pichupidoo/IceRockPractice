package dev.icerock.education.practicetask.data.entities

import android.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class Repo(
    val name: String,
    val id: Int,
    val owner: UserInfo,
    val description: String?,
    val language: String?,
) {
    val color: Int
        get() = LanguageColors.colorsOfLanguages[language] ?: Color.BLACK
}
/*
@startuml
class MainActivity
class DetailInfoFragment
class DetailInfoViewModel
class AuthFragment
class AuthViewModel
class RepositoriesListAdapter
class RepositoriesListFragment
class RepositoriesListViewModel
class AppRepository
class KeyValueStorage
interface GitHubAPI
class Repo
class RepoDetails
class RepoReadme
class UserInfo
class LanguageColors
class ErrorDialogFragment

MainActivity ----> DetailInfoFragment
MainActivity ----> AuthFragment
MainActivity ----> RepositoriesListFragment
DetailInfoFragment --> DetailInfoViewModel
AuthFragment --> AuthViewModel
RepositoriesListFragment --> RepositoriesListViewModel
ErrorDialogFragment --> AuthViewModel
Repo --> RepositoriesListFragment
Repo --> RepositoriesListAdapter
RepositoriesListAdapter -> RepositoriesListFragment
RepoDetails --> DetailInfoFragment
RepoReadme --> DetailInfoFragment
Repo <- LanguageColors
UserInfo -> Repo
RepositoriesListViewModel --> AppRepository
AuthViewModel --> AppRepository
DetailInfoViewModel --> AppRepository
GitHubAPI <|- AppRepository
AppRepository --> KeyValueStorage
@enduml
*/