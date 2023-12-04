package com.idt.luckycat.api

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET("/status")
    suspend fun getRobotStatus(): RobotStatus

    @POST("/tts")
    suspend fun speakText(
        @Query("text") text: String,
        @Query("lang") language: String,
    )
}

data class RobotStatus(
    val status: Int,
)
