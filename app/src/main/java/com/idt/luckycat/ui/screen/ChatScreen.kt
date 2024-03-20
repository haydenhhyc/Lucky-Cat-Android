package com.idt.luckycat.ui.screen

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onExit()
        }
    }

    ChatScreenContent(
        uiState = uiState,
        onTalk = { viewModel.onTalk() },
        navigateBack = navigateBack,
        onReset = { viewModel.resetRobotStatus() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenContent(
    uiState: ChatUiState,
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🤖",
                    fontSize = 48.sp
                )

                Spacer(modifier = Modifier.width(24.dp))
                Text(
                    text = uiState.chatbotReply,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    maxLines = 3,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                "Chatbot State:",
                fontWeight = FontWeight.Bold
            )

            Text(
                text = uiState.chatbotState.toString(),
                fontWeight = FontWeight.Bold
            )

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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "👀",
                    fontSize = 48.sp
                )

                Spacer(modifier = Modifier.width(24.dp))
                Text(
                    text = uiState.userInput,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                    fontSize = 24.sp
                )
            }
        }
    }
}

@Preview
@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
private fun ChatScreenPreview() {
    LuckyCatAndroidTheme {
        ChatScreenContent(
            uiState = ChatUiState(
                host = "192.168.0.1",
                userInput = "Hello GPT!",
                chatbotReply = "Hello Human!"
            )
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Preview(device = "spec:parent=pixel_5,orientation=landscape", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ChatScreenPreviewNight() {
    LuckyCatAndroidTheme {
        ChatScreenContent(
            uiState = ChatUiState(
                host = "192.168.0.1",
                userInput = "Hello GPT!",
                chatbotReply = "Hello Human!"
            )
        )
    }
}
