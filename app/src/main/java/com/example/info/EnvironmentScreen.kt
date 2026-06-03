package com.example.info

import android.os.Build
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
import androidx.compose.material.icons.filled.Build
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
                        text = Strings.get("environment"),
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
            title = Strings.get("environment"),
            icon = Icons.Default.Phone,
            content = {
                InfoListItem(
                    headline = Strings.get("android_version"),
                    supporting = Build.VERSION.RELEASE ?: "Unknown",
                    icon = Icons.Default.Phone
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoListItem(
                    headline = "Android ID",
                    supporting = DeviceUtils.getAndroidId(context),
                    icon = Icons.Default.Phone
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoListItem(
                    headline = Strings.get("api_level"),
                    supporting = "API ${Build.VERSION.SDK_INT}",
                    icon = Icons.Default.Settings
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoListItem(
                    headline = Strings.get("bootloader"),
                    supporting = DeviceUtils.checkBootloaderStatus(),
                    icon = Icons.Default.Info
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoListItem(
                    headline = Strings.get("jvm_version"),
                    supporting = System.getProperty("java.vm.version") ?: "Unknown",
                    icon = Icons.Default.Settings
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoListItem(
                    headline = Strings.get("build_date"),
                    supporting = DeviceUtils.formatTime(Build.TIME),
                    icon = Icons.Default.Info
                )
            }
        )

        // 处理器与内存
        InfoCard(
            title = Strings.get("soc_info"),
            icon = Icons.Default.Build,
            content = {
                InfoListItem(
                    headline = Strings.get("soc_info"),
                    supporting = DeviceUtils.getSocInfo(),
                    icon = Icons.Default.Info
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoListItem(
                    headline = Strings.get("mem_total"),
                    supporting = DeviceUtils.formatStorageSize(DeviceUtils.getMemoryInfo(context).totalMem),
                    icon = Icons.Default.Info
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoListItem(
                    headline = Strings.get("storage_usage"),
                    supporting = "${DeviceUtils.getStorageUsagePercentage()}%",
                    icon = Icons.Default.Info
                )
            }
        )

        // 设备与硬件
        InfoCard(
            title = Strings.get("model"),
            icon = Icons.Default.Settings,
            content = {
                InfoListItem(
                    headline = Strings.get("manufacturer"),
                    supporting = Build.MANUFACTURER ?: "Unknown",
                    icon = Icons.Default.Info
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoListItem(
                    headline = Strings.get("model"),
                    supporting = Build.MODEL ?: "Unknown",
                    icon = Icons.Default.Info
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoListItem(
                    headline = Strings.get("hardware"),
                    supporting = Build.HARDWARE ?: "Unknown",
                    icon = Icons.Default.Info
                )
            }
        )

        // 内核信息
        InfoCard(
            title = Strings.get("kernel_version"),
            icon = Icons.Default.Info,
            content = {
                InfoListItem(
                    headline = Strings.get("kernel_version"),
                    supporting = System.getProperty("os.version") ?: "Unknown",
                    icon = Icons.Default.Info
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoListItem(
                    headline = Strings.get("kernel_date"),
                    supporting = DeviceUtils.getKernelBuildDate(),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (batteryLevel > 20) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                    Text(
                        text = Strings.get("battery_level"),
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
                color = if (batteryLevel > 20) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}
