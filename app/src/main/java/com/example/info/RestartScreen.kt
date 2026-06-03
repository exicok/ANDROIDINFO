package com.example.info

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RestartScreen(context: Context) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "重启",
                modifier = Modifier.padding(top = 32.dp, bottom = 8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "系统重启控制",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RestartButton(
                        text = "重启应用",
                        onClick = { restartApp(context) }
                    )
                    
                    RestartButton(
                        text = "重启系统 (需要Root)",
                        onClick = { runShellCommand("reboot") }
                    )
                    
                    RestartButton(
                        text = "重启界面 (需要Root)",
                        onClick = { runShellCommand("pkill -l TERM com.android.systemui") }
                    )
                    
                    RestartButton(
                        text = "重启至 Recovery (需要Root)",
                        onClick = { runShellCommand("reboot recovery") }
                    )
                    
                    RestartButton(
                        text = "重启至 Bootloader (需要Root)",
                        onClick = { runShellCommand("reboot bootloader") }
                    )
                }
            }
            
            Text(
                text = "提示: 系统级重启操作通常需要设备已 Root。",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun RestartButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = text)
        }
    }
}

fun restartApp(context: Context) {
    val packageManager = context.packageManager
    val intent = packageManager.getLaunchIntentForPackage(context.packageName)
    intent?.let {
        val componentName = it.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }
}

fun runShellCommand(command: String) {
    try {
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
        process.waitFor()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
