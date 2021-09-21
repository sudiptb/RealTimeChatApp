package com.plcoding.streamchatapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.streamchatapp.util.Constants.MIN_USERNAME_LENGTH
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val client: ChatClient
): ViewModel() {

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent get() = _loginEvent.asSharedFlow()

    private fun isValidUsername(username: String) =
        username.length >= MIN_USERNAME_LENGTH

    fun connectUser(username: String) {
        val trimmedUserName = username.trim()
        viewModelScope.launch {
            if (isValidUsername(trimmedUserName)){
                val result = client.connectGuestUser(
                    userId = trimmedUserName,
                    username = trimmedUserName
                ).await()
                if (result.isError){
                    _loginEvent.emit(LoginEvent.ErrorLogin(result.error().message ?: "Unknown error"))
                    return@launch
                }
                _loginEvent.emit(LoginEvent.Success)
            }else{
                _loginEvent.emit(LoginEvent.ErrorInputTooShort)
            }
        }
    }

    sealed class LoginEvent{
        object ErrorInputTooShort: LoginEvent()
        data class ErrorLogin(val error: String): LoginEvent()
        object Success: LoginEvent()
    }

}