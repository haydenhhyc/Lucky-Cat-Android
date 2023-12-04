package com.idt.luckycat.speech.stt

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.app.ActivityCompat

class VoiceRecorder @Throws(VoiceRecorderException::class)
constructor(val context: Context) {

    companion object {
        val SAMPLE_RATE_CANDIDATES = intArrayOf(16000, 11025, 22050, 44100)
        const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
        const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
        private const val TAG = "VoiceRecorder"
    }

    private val audioRecord: AudioRecord
    val listeners = mutableListOf<Listener>()
    val buffer: ByteArray
    val sampleRate: Int
        get() = audioRecord.sampleRate

    init {
        // check permission
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw VoiceRecorderException("Permission for audio recording not granted")
        }

        // find valid sample rate & buffer size
        val sampleRate = SAMPLE_RATE_CANDIDATES.firstOrNull {
            AudioRecord.getMinBufferSize(it, CHANNEL, ENCODING) != AudioRecord.ERROR_BAD_VALUE
        } ?: throw VoiceRecorderException(
            "No valid sample rate for AudioRecord, tried: ${
                SAMPLE_RATE_CANDIDATES.joinToString(
                    ",'"
                )
            }"
        )

        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, CHANNEL, ENCODING) * 10

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            CHANNEL,
            ENCODING,
            bufferSize
        )

        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            throw VoiceRecorderException("AudioRecord failed to initialize")
        }

        buffer = ByteArray(bufferSize)

        // use period = bufferSize / 2, so that it start reading before buffer is full
        // divide by 2 because frame size = 2 for 16bit
        val periodInFrames = bufferSize / 2 / 2
        audioRecord.positionNotificationPeriod = periodInFrames

        // setup listeners
        audioRecord.setRecordPositionUpdateListener(object :
            AudioRecord.OnRecordPositionUpdateListener {
            override fun onMarkerReached(p0: AudioRecord?) {}

            override fun onPeriodicNotification(audioRecord: AudioRecord) {
                // get audio chunk from buffer
                val readCount =
                    audioRecord.read(buffer, 0, buffer.size, AudioRecord.READ_NON_BLOCKING)

                // notify listeners
                listeners.forEach { it.onVoice(buffer, readCount) }
            }
        })
    }

    interface Listener {
        /**
         * Called when the recorder starts hearing voice.
         */
        fun onVoiceStart()

        /**
         * Called when the recorder is hearing voice.
         *
         * @param data The audio data in [AudioFormat.ENCODING_PCM_16BIT].
         * @param size The size of the actual data in `data`.
         */
        fun onVoice(data: ByteArray, size: Int)

        /**
         * Called when the recorder stops hearing voice.
         */
        fun onVoiceEnd()
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    /** start capturing audio input */
    fun start() {
        Log.d(TAG, "start()")
        audioRecord.startRecording()
        listeners.forEach { it.onVoiceStart() }
    }

    /** stop capturing audio input */
    fun stop() {
        Log.d(TAG, "stop()")

        if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
            audioRecord.stop()
        }

        listeners.forEach { it.onVoiceEnd() }
    }

    /** release acquired resources */
    fun release() {
        Log.d(TAG, "release()")
        stop()
        audioRecord.release()
    }
}