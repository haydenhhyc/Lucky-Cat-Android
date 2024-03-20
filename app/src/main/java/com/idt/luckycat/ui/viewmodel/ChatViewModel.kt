package com.idt.luckycat.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.idt.hkcs.conversation.stt.STT
import com.idt.luckycat.BuildConfig
import com.idt.luckycat.api.Response
import com.idt.luckycat.api.RobotApiService
import com.idt.luckycat.api.ServerApiService
import com.idt.luckycat.speech.chatbot.ChatGPTChatModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChatViewModel(
    app: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(app) {
    companion object {
        const val USERNAME = BuildConfig.USERNAME
        const val PASSWORD = BuildConfig.PASSWORD

        const val TAG = "ChatViewModel"
    }

    private val host: String = checkNotNull(savedStateHandle["host"])

    private val _uiState = MutableStateFlow(
        ChatUiState(
            host = host,
        )
    )
    val uiState = _uiState.asStateFlow()
    private val robotApiService: RobotApiService
    private val serverApiService: ServerApiService
    private val stt: STT?
    private val model: ChatGPTChatModel

    init {
        robotApiService = initRobotApiService()
        serverApiService = initServerApiService()
        stt = getSTT()
        model = ChatGPTChatModel(
            apiService = serverApiService,
            historyDepth = 6,
            promptSetId = null
        )
    }

    private fun initRobotApiService(): RobotApiService {
        return RobotApiService(host)
    }

    private fun initServerApiService(): ServerApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(Level.BASIC))
            .authenticator { route, response ->
                if (response.request.header("Authorization") != null) {
                    return@authenticator null
                }

                val credential = Credentials.basic(USERNAME, PASSWORD)

                response.request.newBuilder()
                    .header("Authorization", credential)
                    .build()
            }
            .build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl("https://chat.idthk.net")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServerApiService::class.java)
    }

    private fun getSTT(): STT {
        val context = getApplication<Application>().applicationContext
        val stt = STT(context, "yue-HK")


        viewModelScope.launch(Dispatchers.IO) {
            try {
                stt.init()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return stt
    }

    fun onTalk() {
        if (stt == null) {
            return
        }

        viewModelScope.launch {
            try {
                startChat()
            } catch (e: Exception) {
                _uiState.update { it.copy(chatbotState = ChatbotState.READY) }
            }
        }
    }

    private suspend fun startChat() = withContext(Dispatchers.IO) {
        _uiState.update {
            it.copy(chatbotState = ChatbotState.LISTENING, userInput = "")
        }

        var userInput = ""

        stt?.startFlow()?.collect { result ->
            _uiState.update {
                it.copy(userInput = result)
            }
            userInput = result
        }
        if (userInput.isBlank()) {
            _uiState.update { it.copy(chatbotState = ChatbotState.READY) }
            return@withContext
        }

        Log.d(TAG, "userInput = $userInput")

        // Process
        _uiState.update {
            it.copy(
                chatbotState = ChatbotState.LOADING,
            )
        }
        val reply = model.getReply(userInput)
        Log.d(TAG, "reply = $reply")

        // Speak
        robotApiService.sendMessage(
            text = reply,
            language = "yue-HK"
        )
        _uiState.update {
            it.copy(chatbotState = ChatbotState.SPEAKING)
        }

        // wait for speech finish
        robotApiService.getMessageFlow()
            .filterIsInstance<Response.TTSResponse>()
            .transformWhile { response ->
                emit(response)

                // collect flow until get we a TTSResponse and its state == "end"
                response.state != "end"
            }
            .collect { response ->
                when (response.state) {
                    "start" -> {
                        _uiState.update {
                            it.copy(
                                chatbotState = ChatbotState.SPEAKING,
                                chatbotReply = reply
                            )
                        }
                    }

                    "end" -> {
                        _uiState.update { it.copy(chatbotState = ChatbotState.READY) }
                    }
                }
            }

        // Done
        _uiState.update { it.copy(chatbotState = ChatbotState.READY) }
    }

    fun resetRobotStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // TODO clear robot status
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onExit() {
        robotApiService.disconnect()
    }
}

data class ChatUiState(
    val host: String = "",
    val chatbotState: ChatbotState = ChatbotState.READY,
    val userInput: String = "",
    val chatbotReply: String = "",
) {
    val title = "Chatbot - $host"
    val talkButtonEnabled = (chatbotState == ChatbotState.READY)
}

enum class ChatbotState {
    READY, LISTENING, LOADING, SPEAKING
}