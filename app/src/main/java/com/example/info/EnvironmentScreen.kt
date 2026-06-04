package com.example.info

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import java.util.Locale
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.info.ui.components.InfoCard
import com.example.info.ui.components.InfoListItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EnvironmentScreen(modifier: Modifier = Modifier) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        Strings.get("tab_system"),
        Strings.get("tab_hardware"),
        Strings.get("tab_screen"),
        Strings.get("tab_camera"),
        Strings.get("tab_graphics"),
        Strings.get("tab_sensors"),
        Strings.get("tab_battery"),
        Strings.get("tab_apps")
    )

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
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
                SecondaryScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(text = title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            EnvironmentContent(
                selectedTab = selectedTabIndex,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun EnvironmentContent(selectedTab: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (selectedTab) {
            0 -> SystemTab(context)
            1 -> HardwareTab(context)
            2 -> ScreenTab(context)
            3 -> CameraTab(context)
            4 -> GraphicsTab(context)
            5 -> SensorsTab(context)
            6 -> BatteryTab(context)
            7 -> AppsTab(context)
        }
    }
}

@Composable
fun SystemTab(context: Context) {
    InfoCard(
        title = Strings.get("tab_system"),
        icon = Icons.Default.Phone,
        content = {
            InfoListItem(headline = Strings.get("android_version"), supporting = Build.VERSION.RELEASE ?: "Unknown", icon = Icons.Default.Phone)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = "Android ID", supporting = DeviceUtils.getAndroidId(context), icon = Icons.Default.Phone)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("api_level"), supporting = "API ${Build.VERSION.SDK_INT}", icon = Icons.Default.Settings)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("bootloader"), supporting = DeviceUtils.checkBootloaderStatus(), icon = Icons.Default.Info)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("jvm_version"), supporting = System.getProperty("java.vm.version") ?: "Unknown", icon = Icons.Default.Settings)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("build_date"), supporting = DeviceUtils.formatTime(Build.TIME), icon = Icons.Default.Info)
        }
    )

    InfoCard(
        title = Strings.get("kernel_version"),
        icon = Icons.Default.Info,
        content = {
            InfoListItem(headline = Strings.get("kernel_version"), supporting = System.getProperty("os.version") ?: "Unknown", icon = Icons.Default.Info)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("kernel_date"), supporting = DeviceUtils.getKernelBuildDate(), icon = Icons.Default.Info)
        }
    )
}

@Composable
fun HardwareTab(context: Context) {
    InfoCard(
        title = Strings.get("tab_hardware"),
        icon = Icons.Default.Build,
        content = {
            InfoListItem(headline = Strings.get("soc_info"), supporting = DeviceUtils.getSocInfo(), icon = Icons.Default.Info)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("mem_total"), supporting = DeviceUtils.formatStorageSize(DeviceUtils.getMemoryInfo(context).totalMem), icon = Icons.Default.Info)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("storage_usage"), supporting = DeviceUtils.formatStorageSize(DeviceUtils.getUsedStorageSize(android.os.Environment.getDataDirectory())), icon = Icons.Default.Info)
        }
    )
}

@Composable
fun ScreenTab(context: Context) {
    val info = DeviceUtils.getScreenInfo(context)
    InfoCard(
        title = Strings.get("tab_screen"),
        icon = Icons.Default.Settings,
        content = {
            InfoListItem(headline = Strings.get("screen_res"), supporting = info["resolution"] ?: "N/A", icon = Icons.Default.Info)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("screen_refresh"), supporting = info["refresh_rate"] ?: "N/A", icon = Icons.Default.Info)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("screen_density"), supporting = info["density"] ?: "N/A", icon = Icons.Default.Info)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("screen_size"), supporting = info["size"] ?: "N/A", icon = Icons.Default.Info)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("screen_xdpi"), supporting = info["xdpi_ydpi"] ?: "N/A", icon = Icons.Default.Info)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("screen_features"), supporting = info["features"] ?: "N/A", icon = Icons.Default.Info)
        }
    )
}

@Composable
fun CameraTab(context: Context) {
    val cameras = DeviceUtils.getCameraInfo(context)
    cameras.forEach { cam ->
        InfoCard(
            title = "${Strings.get("tab_camera")} ${cam.id} (${cam.facing})",
            icon = Icons.Default.Phone,
            content = {
                InfoListItem(headline = Strings.get("camera_megapixels"), supporting = cam.megapixels, icon = Icons.Default.Info)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoListItem(headline = Strings.get("camera_aperture"), supporting = cam.aperture, icon = Icons.Default.Info)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoListItem(headline = Strings.get("camera_focal"), supporting = cam.focalLengths, icon = Icons.Default.Info)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoListItem(headline = Strings.get("camera_sensor"), supporting = cam.sensorSize, icon = Icons.Default.Info)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoListItem(headline = Strings.get("camera_iso"), supporting = cam.isoRange, icon = Icons.Default.Info)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoListItem(headline = Strings.get("camera_zoom"), supporting = cam.zoomRange, icon = Icons.Default.Info, iconTint = if (cam.zoomRange == "不支持") Color.Red else MaterialTheme.colorScheme.onSurfaceVariant)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoListItem(headline = Strings.get("camera_af"), supporting = cam.autoFocus, icon = Icons.Default.Info, iconTint = if (cam.autoFocus == "不支持") Color.Red else MaterialTheme.colorScheme.onSurfaceVariant)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoListItem(headline = Strings.get("camera_stab"), supporting = cam.videoStabilization, icon = Icons.Default.Info, iconTint = if (cam.videoStabilization == "不支持") Color.Red else MaterialTheme.colorScheme.onSurfaceVariant)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoListItem(headline = Strings.get("camera_flash"), supporting = cam.flashSupport, icon = Icons.Default.Info, iconTint = if (cam.flashSupport == "不支持") Color.Red else MaterialTheme.colorScheme.onSurfaceVariant)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoListItem(headline = Strings.get("camera_raw"), supporting = cam.rawSupport, icon = Icons.Default.Info, iconTint = if (cam.rawSupport == "不支持") Color.Red else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        )
    }
}

@Composable
fun GraphicsTab(context: Context) {
    InfoCard(
        title = Strings.get("gpu_info"),
        icon = Icons.Default.Build,
        content = {
            InfoListItem(headline = Strings.get("opengl_version"), supporting = DeviceUtils.getGlEsVersion(context), icon = Icons.Default.Info)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("vulkan_version"), supporting = DeviceUtils.getVulkanVersion(context), icon = Icons.Default.Info)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("opencl_version"), supporting = DeviceUtils.getOpenCLVersion(), icon = Icons.Default.Info)
        }
    )
}

@Composable
fun SensorsTab(context: Context) {
    val sensors = remember { DeviceUtils.getSensors(context) }
    var selectedSensorForData by remember { mutableStateOf<SensorInfo?>(null) }

    InfoCard(
        title = Strings.get("sensor_info") + " (${sensors.size})",
        icon = Icons.Default.Info,
        content = {
            sensors.forEachIndexed { index, sensor ->
                InfoListItem(
                    headline = sensor.name,
                    supporting = "${sensor.vendor} - v${sensor.version}",
                    icon = Icons.Default.Info,
                    trailingContent = {
                        IconButton(onClick = { selectedSensorForData = sensor }) {
                            Icon(imageVector = Icons.Default.Visibility, contentDescription = Strings.get("view_data"), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
                if (index < sensors.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    )

    if (selectedSensorForData != null) {
        SensorDataDialog(sensorInfo = selectedSensorForData!!, onDismiss = { selectedSensorForData = null })
    }
}

@Composable
fun BatteryTab(context: Context) {
    val batteryIntent = DeviceUtils.getBatteryIntent(context)
    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val health = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
    val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val batteryPct = if (level != -1 && scale != -1) (level / scale.toFloat() * 100).toInt() else -1
    val voltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
    val temp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
    val tech = batteryIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

    val healthStr = when (health) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
        BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
        else -> "Unknown"
    }

    val statusStr = when (status) {
        BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
        BatteryManager.BATTERY_STATUS_FULL -> "Full"
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
        else -> "Unknown"
    }

    BatteryCard(batteryLevel = batteryPct)

    InfoCard(
        title = Strings.get("tab_battery"),
        icon = Icons.Default.Phone,
        content = {
            InfoListItem(headline = Strings.get("battery_status"), supporting = "$statusStr ($healthStr)", icon = Icons.Default.Info)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("battery_tech"), supporting = tech, icon = Icons.Default.Info)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("battery_voltage"), supporting = "${voltage} mV", icon = Icons.Default.Info)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("battery_temp"), supporting = "${temp / 10f} °C", icon = Icons.Default.Info)
        }
    )
}

@Composable
fun AppsTab(context: Context) {
    val allApps = remember { DeviceUtils.getInstalledApps(context) }
    var showSystemApps by remember { mutableStateOf(false) }
    val filteredApps = allApps.filter { it.isSystemApp == showSystemApps }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { showSystemApps = false }) {
                Text(
                    text = Strings.get("app_user"),
                    color = if (!showSystemApps) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (!showSystemApps) FontWeight.Bold else FontWeight.Normal
                )
            }
            TextButton(onClick = { showSystemApps = true }) {
                Text(
                    text = Strings.get("app_system"),
                    color = if (showSystemApps) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (showSystemApps) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        InfoCard(
            title = if (showSystemApps) Strings.get("app_system") else Strings.get("app_user"),
            icon = Icons.Default.Settings,
            content = {
                filteredApps.forEachIndexed { index, app ->
                    AppListItem(context, app)
                    if (index < filteredApps.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        )
    }
}

@Composable
fun AppListItem(context: Context, app: AppInfo) {
    val pm = context.packageManager
    ListItem(
        headlineContent = { Text(app.name) },
        supportingContent = { Text("${app.packageName}\nv${app.versionName} (${app.versionCode})") },
        leadingContent = {
            val bitmap = remember(app.icon) { app.icon?.toBitmap()?.asImageBitmap() }
            if (bitmap != null) {
                Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.size(40.dp))
            } else {
                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(40.dp))
            }
        },
        trailingContent = {
            Row {
                IconButton(onClick = {
                    val intent = pm.getLaunchIntentForPackage(app.packageName)
                    if (intent != null) context.startActivity(intent)
                }) {
                    Icon(Icons.Default.PlayArrow, contentDescription = Strings.get("open"), tint = MaterialTheme.colorScheme.primary)
                }
                if (!app.isSystemApp) {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_DELETE, Uri.parse("package:${app.packageName}"))
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = Strings.get("uninstall"), tint = Color.Red)
                    }
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun SensorDataDialog(sensorInfo: SensorInfo, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensor = remember { sensorManager.getDefaultSensor(sensorInfo.type) }
    var sensorValues by remember { mutableStateOf(floatArrayOf()) }

    DisposableEffect(sensor) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) { sensorValues = event.values.copyOf() }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (sensor != null) sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(Strings.get("close")) } },
        title = { Text(text = sensorInfo.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = Strings.get("sensor_data"), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                if (sensorValues.isEmpty()) {
                    Text(text = "Waiting for data...")
                } else {
                    sensorValues.forEachIndexed { index, value ->
                        val formattedValue = String.format(Locale.getDefault(), "%.3f", value)
                        Text(text = "Value[$index]: $formattedValue", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    )
}

@Composable
fun BatteryCard(batteryLevel: Int) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(24.dp), tint = if (batteryLevel > 20) Color(0xFF4CAF50) else Color(0xFFF44336))
                    Text(text = Strings.get("battery_level"), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 12.dp), fontWeight = FontWeight.Medium)
                }
                Text(text = "$batteryLevel%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (batteryLevel > 20) Color(0xFF4CAF50) else Color(0xFFF44336) )
            }
            LinearProgressIndicator(progress = { batteryLevel / 100f }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), color = if (batteryLevel > 20) Color(0xFF4CAF50) else Color(0xFFF44336))
        }
    }
}
