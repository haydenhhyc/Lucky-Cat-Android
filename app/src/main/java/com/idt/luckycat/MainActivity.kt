package com.idt.luckycat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.idt.luckycat.nav.MainNavHost
import com.idt.luckycat.ui.PermissionWrapper
import com.idt.luckycat.ui.theme.LuckyCatAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LuckyCatAndroidTheme {
                PermissionWrapper(context = this) {
                    App()
                }
            }
        }
    }
}


@Composable
fun App() {
    val navController = rememberNavController()

    MainNavHost(
        navController = navController,
    )
}