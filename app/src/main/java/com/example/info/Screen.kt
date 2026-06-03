package com.example.info

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen(
    val title: String,
    val icon: ImageVector
) {
    Home(
        title = "主页",
        icon = Icons.Default.Home
    ),
    Environment(
        title = "设备",
        icon = Icons.Default.Settings
    ),
    Restart(
        title = "重启",
        icon = Icons.Default.Refresh
    ),
    Settings(
        title = "设置",
        icon = Icons.Default.Settings
    )
}
