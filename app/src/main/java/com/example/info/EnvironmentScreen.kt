package com.example.info

import android.os.Build
import android.os.Environment
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnvironmentScreen(modifier: Modifier = Modifier) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "环境信息",
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
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            EnvironmentContent(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun EnvironmentContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var batteryLevel by remember { mutableIntStateOf(DeviceUtils.getBatteryLevel(context)) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BatteryCard(batteryLevel = batteryLevel)

        // 系统信息
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
                    supporting = DeviceUtils.getAndroidId(context),
                    icon = Icons.Default.Phone
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                InfoListItem(
                    headline = "Bootloader",
                    supporting = DeviceUtils.checkBootloaderStatus(),
                    icon = Icons.Default.Info
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                InfoListItem(
                    headline = "处理器平台/硬件",
                    supporting = Build.HARDWARE ?: "未知",
                    icon = Icons.Default.Info
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                InfoListItem(
                    headline = "系统发行组织/主机",
                    supporting = Build.HOST ?: "未知",
                    icon = Icons.Default.Info
                )
            }
        )

        // 内核信息
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
                    supporting = DeviceUtils.getKernelBuildDate(),
                    icon = Icons.Default.Info
                )
            }
        )

        // 设备信息
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

        // 构建信息
        InfoCard(
            title = "构建信息",
            icon = Icons.Default.Info,
            content = {
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
                    supporting = DeviceUtils.formatTime(Build.TIME),
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

        // 存储信息
        InfoCard(
            title = "存储信息",
            icon = Icons.Default.Settings,
            content = {
                val externalStorage = Environment.getExternalStorageDirectory()
                val internalStorage = Environment.getDataDirectory()

                InfoListItem(
                    headline = "内部存储/用户 data 路径",
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
                    headline = "存储挂载状态",
                    supporting = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) "已挂载" else "未挂载",
                    icon = Icons.Default.Info
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                InfoListItem(
                    headline = "硬盘大小",
                    supporting = DeviceUtils.formatStorageSize(DeviceUtils.getTotalStorageSize(internalStorage)),
                    icon = Icons.Default.Phone
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                InfoListItem(
                    headline = "已用空间",
                    supporting = DeviceUtils.formatStorageSize(DeviceUtils.getUsedStorageSize(internalStorage)),
                    icon = Icons.Default.Settings
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                InfoListItem(
                    headline = "未用空间",
                    supporting = DeviceUtils.formatStorageSize(DeviceUtils.getFreeStorageSize(internalStorage)),
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
