package com.idt.luckycat.api

import com.idt.luckycat.speech.chatbot.ChatReplyDto
import com.idt.luckycat.speech.chatbot.ChatRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface ServerApiService {
    @POST("api/postchat/")
    suspend fun getChatbotResponse(
        @Body request: ChatRequest,
    ): ChatReplyDto
}
