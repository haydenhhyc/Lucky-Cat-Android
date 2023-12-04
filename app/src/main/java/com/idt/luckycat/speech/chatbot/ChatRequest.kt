package com.idt.luckycat.speech.chatbot

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val message: String,

    @SerializedName("message_history")
    val history: List<Message> = emptyList(),

    val mode: Mode? = Mode.USER_DEFAULT,

    val set: Int? = null,
)
