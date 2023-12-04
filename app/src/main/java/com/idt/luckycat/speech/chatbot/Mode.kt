package com.idt.luckycat.speech.chatbot

import com.google.gson.annotations.SerializedName

enum class Mode {
    /**
     * call the api with no history limit
     */
    @SerializedName("unlimit")
    UNLIMITED,

    /**
     * call the api with user's default system messages
     */
    @SerializedName("user_default")
    USER_DEFAULT,

    @SerializedName("select")
    SELECT,
}