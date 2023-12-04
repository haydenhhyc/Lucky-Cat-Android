package com.idt.luckycat.speech.chatbot

import com.google.gson.annotations.SerializedName

data class PromptSet(
    @SerializedName("pkey")
    val id: Int,
    val name: String,
    val description: String,

    @SerializedName("system_message")
    val messages: List<Message>,
)

