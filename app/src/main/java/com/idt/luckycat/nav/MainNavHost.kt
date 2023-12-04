package com.idt.luckycat.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.idt.luckycat.ui.screen.ChatScreen
import com.idt.luckycat.ui.screen.ConnectScreen


@Composable
fun MainNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "connect",
        modifier = modifier,
    ) {
        composable("connect") {
            ConnectScreen(
                navigateToChat = { host, port ->
                    navController.navigateToChat(host, port)
                }
            )
        }

        composable(
            route = "chat/{host}/{port}",
            arguments = listOf(
                navArgument("host") { type = NavType.StringType },
                navArgument("port") { type = NavType.IntType }
            )
        ) {
            ChatScreen(
                navigateBack = {
                    navController.popBackStack("connect", inclusive = false)
                }
            )
        }
    }
}

fun NavHostController.navigateToChat(host: String, port: Int) {
    navigate("Chat/$host/$port") {
        launchSingleTop = true
    }
}