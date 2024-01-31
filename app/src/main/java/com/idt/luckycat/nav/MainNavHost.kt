package com.idt.luckycat.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.idt.luckycat.camera.ui.CameraScreen
import com.idt.luckycat.camera.ui.CameraUiState
import com.idt.luckycat.ui.screen.ChatScreen
import com.idt.luckycat.ui.screen.ConnectScreen
import com.idt.luckycat.ui.screen.HomeScreen
import com.idt.luckycat.ui.viewmodel.HomeUiState


@Composable
fun MainNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "connect",
        modifier = modifier,
    ) {
        composable("connect") {
            ConnectScreen(
                navigateToHome = { host ->
                    navController.navigate("home/$host") { launchSingleTop = true }
                }
            )
        }

        composable(
            route = "home/{host}",
            arguments = listOf(
                navArgument("host") { type = NavType.StringType })
        ) { entry ->
            val host = entry.arguments?.getString("host") ?: throw IllegalStateException()

            HomeScreen(
                uiState = HomeUiState(host = host),
                navigateBack = {
                    if(navController.currentBackStackEntry == entry) {
                        navController.popBackStack()
                    }
                },
                navigateToChat = {
                    navController.navigate("chat/$host") { launchSingleTop = true }
                },

                navigateToCamera = {
                    navController.navigate("camera/$host") { launchSingleTop = true }
                },
            )
        }

        composable(
            route = "chat/{host}",
            arguments = listOf(
                navArgument("host") { type = NavType.StringType },
            )
        ) {entry ->
            ChatScreen(
                navigateBack = {
                    if(navController.currentBackStackEntry == entry) {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(
            route = "camera/{host}",
            arguments = listOf(
                navArgument("host") { type = NavType.StringType }
            )
        ) {entry ->
            val host = entry.arguments?.getString("host") ?: throw IllegalStateException()

            CameraScreen(
                uiState = CameraUiState(host = host),
                navigateBack = {
                    if(navController.currentBackStackEntry == entry) {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}