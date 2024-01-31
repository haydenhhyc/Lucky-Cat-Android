package com.idt.luckycat.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ConnectViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ConnectUiState())
    val uiState = _uiState.asStateFlow()

    fun onHostInput(value: String) {
        _uiState.update {
            it.copy(hostInput = value)
        }
    }

    fun onClear() {
        _uiState.update {
            it.copy(hostInput = "")
        }
    }

    fun onConnect() {
        // TODO
    }
}

data class ConnectUiState(
    val hostInput: String = "",
) {
    val connectButtonEnabled: Boolean = (hostInput.isNotEmpty())
}