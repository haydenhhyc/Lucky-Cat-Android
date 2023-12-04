package com.idt.luckycat.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RobotApiService {
    @GET("/")
    suspend fun getRobotStatus(): RobotStatus

    @POST("tts")
    suspend fun speakText(
        @Body request: SpeakTextRequest,
    )

    @GET("reset")
    suspend fun resetStatus()
}

data class SpeakTextRequest(
    val text: String,
    val lang: String?,
)

data class RobotStatus(
    val status: Int,
)
