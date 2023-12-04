package com.idt.luckycat.speech.chatbot


data class Message(
    val role: Role,
    val content: String,
)