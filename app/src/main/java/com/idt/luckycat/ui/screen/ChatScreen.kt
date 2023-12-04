package com.idt.luckycat.ui.screen

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.idt.luckycat.ui.theme.LuckyCatAndroidTheme
import com.idt.luckycat.ui.viewmodel.ChatUiState
import com.idt.luckycat.ui.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(),
    navigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    ChatScreenContent(
        uiState = uiState,
        onGetStatus = { viewModel.getRobotStatus() },
        onTalk = { viewModel.onTalk() },
        navigateBack = navigateBack,
        onReset = { viewModel.resetRobotStatus() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenContent(
    uiState: ChatUiState,
    onGetStatus: () -> Unit = {},
    onTalk: () -> Unit = {},
    navigateBack: () -> Unit = {},
    onReset: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = uiState.title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = { navigateBack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Host = ${uiState.host}")
            Text("Port = ${uiState.port}")

            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { onGetStatus() }) {
                Text("GET STATUS")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                enabled = uiState.talkButtonEnabled,
                onClick = { onTalk() }
            ) {
                Text("TALK!")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onReset() }
            ) {
                Text("RESET")
            }
        }
    }
}

@Preview
@Composable
private fun ChatScreenPreview() {
    LuckyCatAndroidTheme {
        ChatScreenContent(
            uiState = ChatUiState(
                host = "192.168.0.1",
                port = 80,
            )
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ChatScreenPreviewNight() {
    LuckyCatAndroidTheme {
        ChatScreenContent(
            uiState = ChatUiState(
                host = "192.168.0.1",
                port = 80,
            )
        )
    }
}
