package com.idt.hkcs.conversation.stt

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.idt.luckycat.speech.stt.CloudSpeechService
import com.idt.luckycat.speech.stt.CloudSpeechServiceException
import com.idt.luckycat.speech.stt.STTException
import com.idt.luckycat.speech.stt.VoiceRecorder
import com.idt.luckycat.speech.stt.VoiceRecorderException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/** API for Speech-To-Text functionalities that uses google's cloud speech service
 * wrapper of the classes:
 * - VoiceRecorder: captures audio input from the mic
 * - CloudSpeechService: process the audio input via google cloud services and convert them into text
 *
 * Usage:
 * 1. Create a STT object
 * 2. Attach listeners to the STT object. You can override following methods:
 *  - onReady()
 *  - onResult()
 *  - onResultFinal()
 *  3. Call STT.start()
 *
 * You don't have to call STT.stop() explicitly, but you can. By default, it will stop automatically when the recognition result is final.
 * Make sure to call STT.release() to release acquired resources.
 */
class STT(
    /** Android context */
    val context: Context,

    /** BCP-47 language code of the supplied audio */
    val language: String = "yue-HK",
) {

    companion object {
        private const val TAG = "STT"
    }

    private val listeners: MutableList<Listener> = arrayListOf()

    lateinit var voiceRecorder: VoiceRecorder
    lateinit var cloudSpeechService: CloudSpeechService
    lateinit var serviceConnection: ServiceConnection

    var state = State.INITIALIZING

    private var interruped = false

    @Throws(STTException::class)
    suspend fun init() {
        try {
            initVoiceRecorder()
            initCloudSpeechService()

        } catch (e: STTException) {
            // init failed, release resources
            release()
            throw e
        }
    }

    @Throws(VoiceRecorderException::class)
    private fun initVoiceRecorder() {
        voiceRecorder = VoiceRecorder(context)
    }

    @Throws(CloudSpeechServiceException::class)
    private suspend fun initCloudSpeechService() {
        return suspendCoroutine { continuation ->
            val cloudSpeechServiceListener = object : CloudSpeechService.Listener {
                override fun onSpeechRecognized(text: String, isFinal: Boolean) {
                    // ignore result if interrupted
                    if (interruped) return

                    listeners.forEach { it.onResult(text, isFinal) }

                    // if result is final, stop recording
                    if (isFinal) {
                        listeners.forEach { it.onResultFinal(text) }
                        stop()
                    }
                }

                override fun onError(message: String) {
                    // ignore if interrupted
                    if (interruped) return

                    listeners.forEach { it.onError(message) }
                    stop()
                }
            }

            serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
                    cloudSpeechService = CloudSpeechService.from(binder)
                    cloudSpeechService.addListener(cloudSpeechServiceListener)
                    setupCommunication()

                    // now the service is ready
                    continuation.resume(Unit)
                    Log.d(TAG, "Service Ready")
                    state = State.READY
                }

                override fun onServiceDisconnected(p0: ComponentName?) {
                    Log.d(TAG, "onServiceDisconnected: ")
                    release()
                }
            }

            val bound = context.bindService(
                Intent(context, CloudSpeechService::class.java),
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )

            if (!bound) {
                throw CloudSpeechServiceException("Failed to bind CloudSpeechService")
            }
        }
    }

    /** setup communication between voiceRecorder and cloudSpeechService */
    fun setupCommunication() {
        voiceRecorder.addListener(object : VoiceRecorder.Listener {
            override fun onVoiceStart() {
                Log.d(TAG, "onVoiceStart: ")
                cloudSpeechService.startRecognizing(voiceRecorder.sampleRate, language)
            }

            override fun onVoice(data: ByteArray, size: Int) {
                if (state != State.RUNNING) return
                cloudSpeechService.recognize(data, size)
            }

            override fun onVoiceEnd() {
                Log.d(TAG, "onVoiceEnd: ")
                cloudSpeechService.finishRecognizing()
                state = State.READY
            }
        })
    }

    interface Listener {
        fun onResult(text: String, isFinal: Boolean)
        fun onResultFinal(text: String)
        fun onError(message: String)
        fun onInterrupted() = Unit
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun removeListeners() {
        listeners.clear()
    }

    /**
     * Callback-based implementation of starting speech recognition
     */
    fun start() {
        if (state != State.READY) return

        state = State.RUNNING
        interruped = false
        voiceRecorder.start()
    }

    /**
     * Flow implementation of starting speech recognition
     */
    @Throws(STTException::class)
    suspend fun startFlow(): Flow<String> = callbackFlow {
        val sttListener = object : Listener {
            override fun onResult(text: String, isFinal: Boolean) {
                Log.d(TAG, "onResult: $text")
                trySend(text)
            }

            override fun onResultFinal(text: String) {
                Log.d(TAG, "onResultFinal: $text")
                trySend(text)
                close()
            }

            override fun onError(message: String) {
                Log.d(TAG, "onError: $message")
                close(STTException("STT Error: $message"))
            }

            override fun onInterrupted() {
                Log.d(TAG, "onInterrupted:")
                close()
            }
        }

        addListener(sttListener)
        start()

        awaitClose { removeListener(sttListener) }
    }

    /**
     * stop gracefully; stt result will still be available
     */
    fun stop() {
        if (state != State.RUNNING) return

        state = State.READY
        voiceRecorder.stop()
    }

    /**
     * stop stt immediately, discarding results
     */
    fun interrupt() {
        interruped = true
        stop()
        listeners.forEach { it.onInterrupted() }
    }

    fun release() {
        stop()

        try {
            voiceRecorder.release()
            context.unbindService(serviceConnection)
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }

        state = State.RELEASED
    }

    enum class State {
        /** when it is initializing and is not ready */
        INITIALIZING,

        /** when STT is initialized and ready to recognize speeches */
        READY,

        /** when STT is recognizing */
        RUNNING,

        /** when resources are released; This object should not be used at this point */
        RELEASED,
    }
}