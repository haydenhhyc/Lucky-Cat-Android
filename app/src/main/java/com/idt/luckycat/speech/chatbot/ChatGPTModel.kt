package com.idt.luckycat.speech.chatbot

import android.util.Log
import com.google.gson.annotations.SerializedName
import com.idt.luckycat.api.ServerApiService
import retrofit2.HttpException
import java.io.IOException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class ChatGPTChatModel(
    val apiService: ServerApiService,

    /**
     * history depth is the maximum number of previous messages which can be stored in the history.
     * Both user and assistant messages are counted toward this limit
     *
     * For example, a historyDepth of 6 means 3 user messages and 3 reply messages are remembered
     */
    val historyDepth: Int,
    val promptSetId: Int?,
) {
    companion object {
        private const val TAG = "ChatGPTChatBot"
    }

    private var history: ArrayDeque<Message> by dequeLimiter(historyDepth)

    @Throws(
        IOException::class,
        HttpException::class,
        ChatGPTException::class
    )
    suspend fun getReply(text: String): String {
        val languagePrompt = Message(
            Role.SYSTEM,
            "always answer in spoken cantonese"
        )

        val chatRequest = promptSetId?.let {
            ChatRequest(
                message = text,
                mode = Mode.SELECT,
                history = history + languagePrompt,
                set = promptSetId
            )
        } ?: ChatRequest(
            message = text,
            mode = Mode.USER_DEFAULT,
            history = history + languagePrompt
        )

        val chatResponse = apiService.getChatbotResponse(chatRequest)

        val replyMessage = chatResponse.choices.firstOrNull()?.message?.content
            ?: throw ChatGPTException("error calling chat server api")

        Log.d(TAG, "ChatGPT reply: $replyMessage")

        // save messages to the history
        history.addAll(
            listOf(
                Message(Role.USER, text),
                Message(Role.ASSISTANT, replyMessage)
            )
        )

        return replyMessage
    }
}

data class ChatReplyDto(
    val choices: List<ChoiceDto>,
)

data class ChoiceDto(
    val message: Message,

    @SerializedName("finished_reason")
    val finishReason: String,
)


class ChatGPTException(message: String) : Exception(message)

/**
 *  A delegate which limits the capacity of a [ArrayDeque]
 *  By using this delegate we can make a fixed-length deque which will
 *  hold elements up to its maximum capacity, and will discard older elements
 *  when exceeding the capacity
 *
 *  Usage:
 *  ```
 *  // create a deque, and use our property delegate to intercept
 *  // invocations and limit the size to 3
 *  val limitedDeque: ArrayDeque<Int> by dequeLimiter(3)
 *  ```
 *  see: [StackOverFlow post](https://stackoverflow.com/a/69074589)
 */
fun <E> dequeLimiter(limit: Int): ReadWriteProperty<Any?, ArrayDeque<E>> =
    object : ReadWriteProperty<Any?, ArrayDeque<E>> {

        private var deque: ArrayDeque<E> = ArrayDeque(limit)

        private fun applyLimit() {
            while (deque.size > limit) {
                deque.removeFirst()
            }
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): ArrayDeque<E> {
            applyLimit()
            return deque
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: ArrayDeque<E>) {
            this.deque = value
            applyLimit()
        }
    }
