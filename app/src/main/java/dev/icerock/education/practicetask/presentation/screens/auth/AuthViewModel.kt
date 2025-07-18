package dev.icerock.education.practicetask.presentation.screens.auth

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dev.icerock.education.practicetask.BuildConfig
import dev.icerock.education.practicetask.data.repository.AppRepository
import dev.icerock.education.practicetask.error_types.ApiError
import dev.icerock.education.practicetask.error_types.ErrorType
import javax.inject.Inject

private const val TAG = "AuthViewModel"

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AppRepository,
) : ViewModel() {

    private val tokenValidationPattern = "^[a-z_0-9]+$".toRegex(RegexOption.IGNORE_CASE)

    var token = MutableLiveData<String>()
        private set

    var state = MutableLiveData<State>()
        private set


    private val _actions: Channel<Action> = Channel(Channel.BUFFERED)
    val actions: Flow<Action> = _actions.receiveAsFlow()

    private var signInJob: Job? = null

    sealed interface State {
        object Idle : State
        object Loading : State
        object InvalidInput : State
    }

    sealed interface Action {
        data class ShowError(
            val error: ApiError,
            val message: String? = null,
            val httpCode: Int? = null,
        ) : Action

        object RouteToMain : Action
    }

    init {
        if (repository.isAuthorized) _actions.trySend(Action.RouteToMain)
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            _actions.send(
                Action.ShowError(
                    error = ApiError.NetworkError(ErrorType.NetworkError),
                    message = throwable.localizedMessage
                )
            )
        }
    }

    fun onSignInButtonPressed() {
        signInJob = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            state.postValue(State.Loading)

            val matches = token.value.toString().matches(tokenValidationPattern)
            Log.i(TAG, "Matches: $matches")
            if (!token.value.toString().matches(tokenValidationPattern)) {
                state.postValue(State.InvalidInput)
                return@launch
            }

            val response = repository.signIn(token.value.toString())
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    state.postValue(State.Idle)
                    _actions.send(Action.RouteToMain)
                    return@withContext
                }

                if (response.code() == 401) {
                    state.postValue(State.InvalidInput)
                    return@withContext
                }
                if (BuildConfig.DEBUG) Log.i(TAG, "Response code: ${response.code()}")
                _actions.send(
                    Action.ShowError(
                        error = ApiError.NetworkError(ErrorType.HttpError(response.code())),
                        httpCode = response.code()
                    )
                )
                state.postValue(State.Idle)
            }
        }
    }

    fun enterToken(token: String) {
        this.token.value = token
    }

    override fun onCleared() {
        super.onCleared()
        signInJob?.cancel()
    }
}