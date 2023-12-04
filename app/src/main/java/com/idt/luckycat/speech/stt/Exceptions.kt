package com.idt.luckycat.speech.stt

/** base class for custom stt related exceptions */
open class STTException(message: String = "") : Exception(message)

/** exceptions specific to the VoiceRecorder class */
open class VoiceRecorderException(message: String = "") : STTException(message)

/** exceptions specific to the CloudSpeechService class */
open class CloudSpeechServiceException(message: String = "") : STTException(message)
