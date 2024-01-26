package com.idt.luckycat.camera.ui

import android.graphics.Bitmap
import android.media.MediaFormat
import android.view.TextureView
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.video.VideoFrameMetadataListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

const val RtspUrl = "rtsp://192.168.0.72:8554/camera"

@Composable
fun CameraScreen() {
    CameraScreenContent(
        onFrame = { bitmap ->
        },
        overlay = {

        }
    )
}

@OptIn(UnstableApi::class)
@Composable
fun CameraScreenContent(
    url: String = RtspUrl,
    onFrame: (Bitmap) -> Unit = {},
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Box(
            modifier = Modifier
                .width(640.dp)
                .height(360.dp)
        ) {
            AndroidView(
                factory = { context ->
                    val tv = TextureView(context)
                    val loadControl = DefaultLoadControl.Builder()
                        .setBufferDurationsMs(
                            100,
                            50_000,
                            100,
                            100
                        )
                        .build()

                    val player = ExoPlayer.Builder(context)
                        .setLoadControl(loadControl)
                        .build()
                        .apply {
                            setVideoFrameMetadataListener(object : VideoFrameMetadataListener {
                                private val mutex = Mutex()
                                override fun onVideoFrameAboutToBeRendered(
                                    presentationTimeUs: Long,
                                    releaseTimeNs: Long,
                                    format: Format,
                                    mediaFormat: MediaFormat?,
                                ) {
                                    scope.launch(Dispatchers.Default) {
                                        if (mutex.isLocked) {
                                            return@launch
                                        }

                                        mutex.withLock {
                                            val bitmap = tv.bitmap ?: return@withLock
                                            onFrame(bitmap)
                                        }
                                    }
                                }
                            })
                            setVideoTextureView(tv)
                            setMediaItem(MediaItem.fromUri(url))
                            prepare()
                            play()
                        }

                    player.setVideoTextureView(tv)
                    tv
                })

            overlay()
        }
    }
}

@Preview
@Composable
private fun CameraScreenPreview() {

}