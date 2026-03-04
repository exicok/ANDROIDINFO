package com.example.info

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.info.ui.components.InfoCard
import com.example.info.ui.components.InfoListItem
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(context: Context, modifier: Modifier = Modifier) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "设备信息",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        DeviceInfoContent(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        )
    }
}

@Composable
fun DeviceInfoContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var batteryLevel by remember { mutableIntStateOf(getBatteryLevel(context)) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BatteryCard(batteryLevel = batteryLevel)

        // 系统信息卡片（包含系统环境、构建信息）
        InfoCard(
            title = "系统信息",
            icon = Icons.Default.Phone,
            content = {
                InfoListItem(
                    headline = "Android 版本",
                    supporting = Build.VERSION.RELEASE ?: "Unknown",
                    icon = Icons.Default.Phone
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                InfoListItem(
                    headline = "SDK 版本",
                    supporting = "API ${Build.VERSION.SDK_INT}",
                    icon = Icons.Default.Settings
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                InfoListItem(
                    headline = "Android ID",
                    supporting = Build.SERIAL ?: "未知",
                    icon = Icons.Default.Phone
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                InfoListItem(
                    headline = "Bootloader",
                    supporting = Build.BOOTLOADER ?: "未知",
                    icon = Icons.Default.Info
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                InfoListItem(
                    headline = "硬件",
                    supporting = Build.HARDWARE ?: "未知",
                    icon = Icons.Default.Info
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                InfoListItem(
                    headline = "主机",
                    supporting = Build.HOST ?: "未知",
                    icon = Icons.Default.Info
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                InfoListItem(
                    headline = "构建 ID",
                    supporting = Build.ID ?: "未知",
                    icon = Icons.Default.Info
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                InfoListItem(
                    headline = "构建时间",
                    supporting = Build.TIME.toString(),
                    icon = Icons.Default.Info
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                InfoListItem(
                    headline = "构建用户",
                    supporting = Build.USER ?: "未知",
                    icon = Icons.Default.Info
                )
            }
        )

        // 内核信息卡片
        InfoCard(
            title = "内核信息",
            icon = Icons.Default.Settings,
            content = {
                InfoListItem(
                    headline = "内核版本",
                    supporting = System.getProperty("os.version") ?: "Unknown",
                    icon = Icons.Default.Info
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                InfoListItem(
                    headline = "内核编译日期",
                    supporting = getKernelBuildDate(),
                    icon = Icons.Default.Info
                )
            }
        )

        // 设备信息卡片
        InfoCard(
            title = "设备信息",
            icon = Icons.Default.Phone,
            content = {
                InfoListItem(
                    headline = "制造商",
                    supporting = Build.MANUFACTURER ?: "Unknown",
                    icon = Icons.Default.Info
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                InfoListItem(
                    headline = "型号",
                    supporting = Build.MODEL ?: "Unknown",
                    icon = Icons.Default.Info
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                InfoListItem(
                    headline = "设备",
                    supporting = Build.DEVICE ?: "Unknown",
                    icon = Icons.Default.Info
                )
            }
        )

        // 存储信息卡片
        InfoCard(
            title = "存储信息",
            icon = Icons.Default.Settings,
            content = {
                val externalStorage = Environment.getExternalStorageDirectory()
                val internalStorage = Environment.getDataDirectory()

                InfoListItem(
                    headline = "用户数据路径",
                    supporting = internalStorage.absolutePath,
                    icon = Icons.Default.Phone
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                InfoListItem(
                    headline = "外部存储路径",
                    supporting = externalStorage?.absolutePath ?: "不可用",
                    icon = Icons.Default.Settings
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                InfoListItem(
                    headline = "内部存储状态",
                    supporting = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) "已挂载" else "未挂载",
                    icon = Icons.Default.Info
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                InfoListItem(
                    headline = "硬盘大小",
                    supporting = formatStorageSize(getTotalStorageSize(internalStorage)),
                    icon = Icons.Default.Phone
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                InfoListItem(
                    headline = "已用空间",
                    supporting = formatStorageSize(getUsedStorageSize(internalStorage)),
                    icon = Icons.Default.Settings
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                InfoListItem(
                    headline = "未用空间",
                    supporting = formatStorageSize(getFreeStorageSize(internalStorage)),
                    icon = Icons.Default.Info
                )
            }
        )
    }
}

@Composable
fun BatteryCard(batteryLevel: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "电池",
                        modifier = Modifier.size(24.dp),
                        tint = if (batteryLevel > 20) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                    Text(
                        text = "电池电量",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 12.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "$batteryLevel%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (batteryLevel > 20) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
            LinearProgressIndicator(
                progress = { batteryLevel / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                color = if (batteryLevel > 20) Color(0xFF4CAF50) else Color(0xFFF44336),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

fun getBatteryLevel(context: Context): Int {
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
}

fun getKernelBuildDate(): String {
    return try {
        val kernelVersion = System.getProperty("os.version") ?: ""
        val parts = kernelVersion.split(" ")
        for (part in parts) {
            if (part.matches(Regex("\\d{2,4}-\\d{2}-\\d{2}"))) {
                return part
            }
        }
        "未知"
    } catch (e: Exception) {
        "未知"
    }
}

fun getTotalStorageSize(directory: File): Long {
    return try {
        val stat = StatFs(directory.absolutePath)
        stat.totalBytes
    } catch (e: Exception) {
        0L
    }
}

fun getUsedStorageSize(directory: File): Long {
    return try {
        val stat = StatFs(directory.absolutePath)
        stat.totalBytes - stat.availableBytes
    } catch (e: Exception) {
        0L
    }
}

fun getFreeStorageSize(directory: File): Long {
    return try {
        val stat = StatFs(directory.absolutePath)
        stat.availableBytes
    } catch (e: Exception) {
        0L
    }
}

fun formatStorageSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    
    return String.format("%.2f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
