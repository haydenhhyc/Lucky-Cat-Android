package com.idt.luckycat.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow

class HomeViewModel(
    app:Application,
    savedStateHandle: SavedStateHandle,
):AndroidViewModel(app) {
    private val host: String = checkNotNull(savedStateHandle["host"])
    private val _uiState = MutableStateFlow(HomeUiState(
        host = host
    ))

}

data class HomeUiState(
    val host:String = "127.0.0.1"
)