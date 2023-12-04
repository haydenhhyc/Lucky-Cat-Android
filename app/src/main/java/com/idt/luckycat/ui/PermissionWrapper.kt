package com.idt.luckycat.ui

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionWrapper(
    context: Context,
    content: @Composable () -> Unit
) {
    val permissions =context.packageManager.getPackageInfo(
        context.packageName,
        PackageManager.GET_PERMISSIONS
    ).requestedPermissions.toList()

    val permissionsState = rememberMultiplePermissionsState(permissions = permissions)

    SideEffect {
        permissionsState.launchMultiplePermissionRequest()
    }

    if (permissionsState.allPermissionsGranted) {
        content()
    }
}