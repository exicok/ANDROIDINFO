package com.example.info

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen(
    val key: String,
    val icon: ImageVector
) {
    Home(
        key = "home",
        icon = Icons.Default.Home
    ),
    Environment(
        key = "environment",
        icon = Icons.Default.Settings
    ),
    Restart(
        key = "restart",
        icon = Icons.Default.Refresh
    ),
    Settings(
        key = "settings",
        icon = Icons.Default.Settings
    );

    val title: String
        @Composable
        get() = Strings.get(key)
}
