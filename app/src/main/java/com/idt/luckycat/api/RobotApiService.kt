package com.idt.luckycat.api

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class RobotApiService(
    private val host: String,
    private val port: Int = 3000,
) {
    private var ws: WebSocketClient = WebSocketClient().apply {
        setSocketUrl("ws://$host:$port/control")
        connect()
    }

    suspend fun getMessageFlow() = callbackFlow {
        ws.setListener(object : WebSocketClient.SocketListener {
            override fun onMessage(message: String) {
                try {
                    val responseJson = JsonParser.parseString(message).asJsonObject
                    val feature = responseJson.get("feature").asString ?: return

                    val response = when (feature) {
                        "tts" -> with(responseJson) {
                            Response.TTSResponse(
                                state = get("state")?.asString,
                                result = get("result")?.asString
                            )
                        }

                        // TODO
                        else -> Response.OtherResponse(message)
                    }

                    trySend(response)

                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                }
            }
        })

        awaitClose {
            ws.removeListener()
        }
    }

    fun sendMessage(text: String, language: String) {
        val message = TTSRequest(
            feature = "tts",
            text = text,
            language = language
        )

        val json = JsonObject().apply {
            with(message) {
                addProperty("feature", feature)
                addProperty("text", text)
                addProperty("lang", language)
            }
        }

        // api only accept arrays so need to wrap it in JsonArray
        val jsonArray = JsonArray().apply {
            add(json)
        }

        ws.sendMessage(jsonArray.toString())
    }

    fun disconnect() {
        ws.disconnect()
    }
}

data class TTSRequest(
    val feature: String?,
    val text: String?,
    val language: String?,
)

sealed interface Response {
    class TTSResponse(
        val state: String?,
        val result: String?,
    ) : Response

    class OtherResponse(
        val content: String
    ) : Response
}
