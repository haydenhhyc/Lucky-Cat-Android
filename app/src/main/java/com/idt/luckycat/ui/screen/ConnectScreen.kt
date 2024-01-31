package com.idt.luckycat.ui.screen

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.idt.luckycat.ui.theme.LuckyCatAndroidTheme
import com.idt.luckycat.ui.viewmodel.ConnectUiState
import com.idt.luckycat.ui.viewmodel.ConnectViewModel

@Composable
fun ConnectScreen(
    viewModel: ConnectViewModel = viewModel(),
    navigateToHome: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    ConnectScreenContent(
        uiState = uiState,
        onHostInput = { viewModel.onHostInput(it) },
        onConnect = {
            viewModel.onConnect()

            try {
                navigateToHome(uiState.hostInput)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        },
        onClear = { viewModel.onClear() }
    )
}

@Composable
fun ConnectScreenContent(
    uiState: ConnectUiState,
    onHostInput: (String) -> Unit = {},
    onConnect: () -> Unit = {},
    onClear: () -> Unit = {},
) {
    Surface {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                value = uiState.hostInput,
                onValueChange = onHostInput,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                label = { Text(text = "Host IP") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                FilledTonalButton(onClick = onClear) {
                    Text("CLEAR", fontWeight = FontWeight.Bold)
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
            uiState = ConnectUiState("192.168.0.123"),
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ConnectScreenPreviewNight() {
    LuckyCatAndroidTheme {
        ConnectScreenContent(
            uiState = ConnectUiState("192.168.0.123"),
        )
    }
}