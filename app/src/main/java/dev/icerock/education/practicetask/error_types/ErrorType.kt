package dev.icerock.education.practicetask.error_types

sealed interface ErrorType {
    object NetworkError : ErrorType
    data class HttpError(val code: Int?) : ErrorType
}
