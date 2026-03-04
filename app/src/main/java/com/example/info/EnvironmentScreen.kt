package com.example.info

import android.os.Build
import android.os.Environment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.info.ui.components.InfoCard
import com.example.info.ui.components.InfoListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnvironmentScreen() {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 存储信息卡片
        InfoCard(
            title = "存储信息",
            icon = Icons.Default.Settings,
            content = {
                val externalStorage = Environment.getExternalStorageDirectory()
                val internalStorage = Environment.getDataDirectory()

                InfoListItem(
                    headline = "内部存储路径",
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
            }
        )

        // 系统环境卡片
        InfoCard(
            title = "系统环境",
            icon = Icons.Default.Settings,
            content = {
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
            }
        )

        // 构建信息卡片
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
    }
}
