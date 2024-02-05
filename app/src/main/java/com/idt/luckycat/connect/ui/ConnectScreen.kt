package com.idt.luckycat.connect.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.idt.luckycat.ui.theme.LuckyCatAndroidTheme

@Composable
fun ConnectScreen(
    viewModel: ConnectViewModel = viewModel(),
    navigateToHome: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    ConnectScreenContent(
        uiState = uiState,
        onEnter = viewModel::onEnter,
        onScan = viewModel::onScan,
        onSelect = viewModel::onSelect,
        onConnect = {
            viewModel.onConnect()
            uiState.hostSelected?.let {
                try {
                    navigateToHome(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        },
    )
}

@Composable
fun ConnectScreenContent(
    uiState: ConnectUiState,
    onEnter: () -> Unit = {},
    onScan: () -> Unit = {},
    onSelect: (String) -> Unit = {},
    onConnect: () -> Unit = {},
) {
    LaunchedEffect(Unit) {
        onEnter()
    }

    Surface {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.6f)
                    .fillMaxWidth(0.8f)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.onPrimaryContainer,
                        RoundedCornerShape(4.dp)
                    )
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(uiState.hosts) { host ->
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (uiState.hostSelected == host) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    Color.Transparent
                                }
                            )
                            .clickable {
                                onSelect(host)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = host,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null
                            )
                        }
                    }
                }

                if (uiState.isScanning) {
                    LinearProgressIndicator(modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                FilledTonalButton(onClick = onScan) {
                    Text("SCAN", fontWeight = FontWeight.Bold)
                }

                Button(onClick = onConnect, enabled = uiState.connectButtonEnabled) {
                    Text(text = "CONNECT!", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectScreenPreview() {
    LuckyCatAndroidTheme {
        ConnectScreenContent(
            uiState = ConnectUiState(
                hosts = listOf(
                    "192.168.0.1",
                    "192.168.0.69"
                ),
                hostSelected = "192.168.0.69"
            ),
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ConnectScreenPreviewNight() {
    ConnectScreenPreview()
    LuckyCatAndroidTheme {
        ConnectScreenContent(
            uiState = ConnectUiState(
                hosts = listOf(
                    "192.168.0.1",
                    "192.168.0.69"
                ),
                hostSelected = "192.168.0.69"
            ),
        )
    }
}