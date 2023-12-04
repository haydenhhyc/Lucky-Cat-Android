package com.idt.luckycat.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idt.luckycat.api.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChatViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val host: String = checkNotNull(savedStateHandle["host"])
    private val port: Int = checkNotNull(savedStateHandle["port"])

    private val _uiState = MutableStateFlow(
        ChatUiState(
            host = host,
            port = port,
        )
    )
    val uiState = _uiState.asStateFlow()
    private val apiService: ApiService = initApiService()

    private fun initApiService(): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(Level.BASIC))
            .build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl("http://${host}:${port}")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun getRobotStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            // TODO
            try {
                apiService.getRobotStatus()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun speakText() {
        val text = "hello"
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.speakText(
                    text = text,
                    language = "en-US"
                )

                Log.d("TAG", "speakText: $response")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

data class ChatUiState(
    val host: String = "",
    val port: Int = 0,
) {
    val title = "$host:$port"
}