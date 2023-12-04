package com.idt.luckycat.speech.chatbot

import com.google.gson.annotations.SerializedName

enum class Role {
    @SerializedName("system")
    SYSTEM,

    @SerializedName("user")
    USER,

    @SerializedName("assistant")
    ASSISTANT
}