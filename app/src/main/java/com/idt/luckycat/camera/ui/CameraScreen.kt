package com.idt.luckycat.camera.ui

import android.graphics.Bitmap
import android.media.MediaFormat
import android.view.TextureView
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.video.VideoFrameMetadataListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.system.measureNanoTime

const val TAG = "CameraScreen"

private fun getRtspUrl(host: String) = "rtsp://$host:8554/camera"

@Composable
fun CameraScreen(
    uiState: CameraUiState = CameraUiState(),
    navigateBack: () -> Unit = {},
) {
    val options = remember {
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
    }

    val detector = remember(options) {
        FaceDetection.getClient(options)
    }

    var faces = remember(detector) {
        mutableStateOf(listOf<Face>())
    }

    CameraScreenContent(
        uiState = uiState,
        navigateBack = navigateBack,
        onFrame = { bitmap ->
            val image = InputImage.fromBitmap(bitmap, 0)
            val time = measureNanoTime {
                detector.process(image)
                    .addOnSuccessListener {
                        Log.d(TAG, "Detection Success - faces : ${it.size}")
                        faces.value = it
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Detection Failed")
                        e.printStackTrace()
                    }
            }
            Log.d(TAG, "Detection time taken: $time ns")
        },
        overlay = {
            val textMeasurer = rememberTextMeasurer()
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                faces.value.forEach { face ->
                    val box = face.boundingBox

                    drawRect(
                        Color.Green,
                        topLeft = Offset(box.left.toFloat(), box.top.toFloat()),
                        size = Size(
                            width = (box.right - box.left).toFloat(),
                            height = (box.bottom - box.top).toFloat()
                        ),
                        style = Stroke(width = 1f)
                    )

                    face.smilingProbability?.let { prob ->
                        Log.d(TAG, "smile: ${face.smilingProbability}")
                        val isSmiling = prob > 0.5f

                        try {
                            translate(
                                left = box.left.toFloat(),
                                top = box.bottom.toFloat()
                            ) {
                                drawText(
                                    textMeasurer,
                                    text = if (isSmiling) "HAPPY" else "SADGE",
                                    style = TextStyle.Default.copy(
                                        color = if (isSmiling) Color.Green else Color.Red,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    )
}

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@OptIn(UnstableApi::class)
@Composable
fun CameraScreenContent(
    uiState: CameraUiState,
    navigateBack: () -> Unit,
    onFrame: (Bitmap) -> Unit = {},
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "Camera - ${uiState.host}") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                navigationIcon = {
                    IconButton(onClick = { navigateBack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "back")
                    }
                })
        }
    ) { innerPadding ->
        val scope = rememberCoroutineScope()

        Box(
            Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(640.dp)
                    .height(480.dp)
            ) {
                AndroidView(
                    factory = { context ->
                        val tv = TextureView(context)
                        val loadControl = DefaultLoadControl.Builder()
                            .setBufferDurationsMs(
                                100,
                                2_000,
                                100,
                                100
                            )
                            .build()

                        val rtspUrl = getRtspUrl(uiState.host)
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
                                setMediaItem(MediaItem.fromUri(rtspUrl))
                                prepare()
                                play()
                            }

                        player.setVideoTextureView(tv)
                        tv
                    }
                )

                overlay()
            }
        }
    }
}

data class CameraUiState(
    val host: String = "127.0.0.1",
)