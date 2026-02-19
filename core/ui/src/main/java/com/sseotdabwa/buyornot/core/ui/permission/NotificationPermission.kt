package com.sseotdabwa.buyornot.core.ui.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * 알림 권한 상태 확인
 */
fun Context.hasNotificationPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        // Android 13 미만에서는 항상 true
        true
    }
}

/**
 * 앱 설정 화면으로 이동
 */
fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    startActivity(intent)
}

/**
 * 알림 권한 요청 및 상태 관리를 위한 Composable
 *
 * @param onPermissionResult 권한 결과 콜백 (granted: Boolean)
 * @return Pair<Boolean, () -> Unit> - (권한 여부, 권한 요청 함수)
 */
@Composable
fun rememberNotificationPermission(
    onPermissionResult: (Boolean) -> Unit = {}
): Pair<Boolean, () -> Unit> {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(context.hasNotificationPermission())
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        onPermissionResult(isGranted)
    }

    val requestPermission: () -> Unit = remember {
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    return Pair(hasPermission, requestPermission)
}

