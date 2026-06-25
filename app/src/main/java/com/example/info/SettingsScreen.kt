package com.example.info

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.info.ui.components.InfoCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.info.ui.viewmodel.SettingsViewModel
import androidx.compose.runtime.collectAsState
import com.example.info.ui.components.InfoListItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val settingsViewModel: SettingsViewModel = viewModel()
    
    val wifiEnabled by settingsViewModel.wifiEnabled.collectAsState()
    val btEnabled by settingsViewModel.btEnabled.collectAsState()
    val nfcEnabled by settingsViewModel.nfcEnabled.collectAsState()
    val gpsEnabled by settingsViewModel.gpsEnabled.collectAsState()
    
    val battPercent by settingsViewModel.battPercent.collectAsState()
    val networkTraffic by settingsViewModel.networkTraffic.collectAsState()
    val doubleTapSleep by settingsViewModel.doubleTapSleep.collectAsState()
    val adbNetwork by settingsViewModel.adbNetwork.collectAsState()

    var showBroadcast by remember { mutableStateOf(false) }

    if (showBroadcast) {
        BroadcastScreen(onBack = { showBroadcast = false })
        return
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Strings.get("settings"),
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoCard(
                    title = Strings.get("theme_settings"),
                    icon = Icons.Default.Settings,
                    content = {
                        ThemeSelectionGroup(
                            currentTheme = currentTheme,
                            onThemeChange = onThemeChange
                        )
                    }
                )

                InfoCard(
                    title = Strings.get("lang_settings"),
                    icon = Icons.Default.Info,
                    content = {
                        LanguageSelectionGroup(
                            currentLanguage = currentLanguage,
                            onLanguageChange = onLanguageChange
                        )
                    }
                )

                TerminalCard()

                InfoCard(
                    title = Strings.get("tab_broadcast"),
                    icon = Icons.Default.Settings,
                    content = {
                        Column(modifier = Modifier.padding(8.dp)) {
                            LinkItem(
                                title = Strings.get("tab_broadcast"),
                                subtitle = Strings.get("broadcast_desc"),
                                onClick = { showBroadcast = true }
                            )
                        }
                    }
                )

                InfoCard(
                    title = Strings.get("about"),
                    icon = Icons.Default.Info,
                    content = {
                        Column(modifier = Modifier.padding(8.dp)) {
                            LinkItem(
                                title = Strings.get("github_title"),
                                subtitle = Strings.get("github_sub"),
                                onClick = { uriHandler.openUri("https://github.com") }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            LinkItem(
                                title = Strings.get("web_title"),
                                subtitle = Strings.get("web_sub"),
                                onClick = { uriHandler.openUri("https://example.com") }
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun LanguageSelectionGroup(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val languageOptions = listOf(
        "zh" to "中文 (简体)",
        "en" to "English"
    )

    Column(Modifier.selectableGroup()) {
        languageOptions.forEachIndexed { index, option ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (option.first == currentLanguage),
                        onClick = { onLanguageChange(option.first) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (option.first == currentLanguage),
                    onClick = null
                )
                Text(
                    text = option.second,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            if (index < languageOptions.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
fun TerminalCard() {
    var command by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    InfoCard(
        title = Strings.get("terminal"),
        icon = Icons.Default.Build,
        content = {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = command,
                    onValueChange = { command = it },
                    label = { Text(Strings.get("shell_hint")) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    val result = withContext(Dispatchers.IO) {
                                        DeviceUtils.executeShell(command, useRoot = true)
                                    }
                                    output = result
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = Strings.get("run"))
                        }
                    },
                    singleLine = true
                )

                if (output.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.shapes.small
                            )
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "${Strings.get("output")}:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = output,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                Text(
                    text = Strings.get("root_hint"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
fun LinkItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = false,
                onClick = onClick,
                role = Role.Button
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ThemeSelectionGroup(
    currentTheme: String,
    onThemeChange: (String) -> Unit
) {
    val radioOptions = listOf(
        "Light" to "浅色模式",
        "Dark" to "深色模式",
        "System" to "跟随系统"
    )

    Column(Modifier.selectableGroup()) {
        radioOptions.forEachIndexed { index, option ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (option.first == currentTheme),
                        onClick = { onThemeChange(option.first) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (option.first == currentTheme),
                    onClick = null
                )
                Text(
                    text = option.second,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            if (index < radioOptions.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}
