package com.example.info

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.hardware.camera2.CameraDevice
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import java.util.Locale
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import android.opengl.Matrix
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import com.example.info.ui.components.InfoCard
import com.example.info.ui.components.InfoListItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EnvironmentScreen(modifier: Modifier = Modifier) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val tabs = listOf(
        Strings.get("tab_system"),
        Strings.get("tab_hardware"),
        Strings.get("tab_processor"),
        Strings.get("tab_screen"),
        Strings.get("tab_camera"),
        Strings.get("tab_graphics"),
        Strings.get("tab_sensors"),
        Strings.get("tab_partitions"),
        Strings.get("tab_battery"),
        Strings.get("tab_apps")
    )
    val pagerState = rememberPagerState { tabs.size }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { 
                                scope.launch { 
                                    pagerState.animateScrollToPage(index) 
                                } 
                            },
                            text = { Text(text = title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            beyondViewportPageCount = 1
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val context = LocalContext.current
                when (page) {
                    0 -> SystemTab(context)
                    1 -> HardwareTab(context)
                    2 -> ProcessorTab(context, snackbarHostState)
                    3 -> ScreenTab(context)
                    4 -> CameraTab(context)
                    5 -> GraphicsTab(context)
                    6 -> SensorsTab(context)
                    7 -> PartitionsTab(context, snackbarHostState)
                    8 -> BatteryTab(context)
                    9 -> AppsTab(context)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProcessorTab(context: Context, snackbarHostState: SnackbarHostState) {
    var cpuCores by remember { mutableStateOf(DeviceUtils.getCpuCoresInfo()) }
    var clusters by remember { mutableStateOf(DeviceUtils.getCpuClustersInfo()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            cpuCores = DeviceUtils.getCpuCoresInfo()
            delay(1000)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Strings.get("processor_control"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${Strings.get("core_count")}: ${DeviceUtils.getCpuCoreCount()}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // CPU Core Grid
        Column(modifier = Modifier.fillMaxWidth()) {
            val coreChunks = cpuCores.chunked(4)
            coreChunks.forEach { chunk ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chunk.forEach { core ->
                        CpuCoreCard(
                            core = core,
                            modifier = Modifier.weight(1f),
                            onToggle = {
                                scope.launch {
                                    val result = DeviceUtils.toggleCoreStatus(core.id, !core.isOnline)
                                    if (result.contains("Error")) {
                                        snackbarHostState.showSnackbar(result)
                                    }
                                    cpuCores = DeviceUtils.getCpuCoresInfo()
                                }
                            }
                        )
                    }
                    // Fill empty space if last row has fewer than 4 items
                    if (chunk.size < 4) {
                        repeat(4 - chunk.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Settings Note
        InfoCard(
            title = Strings.get("single_core_settings"),
            icon = Icons.Default.Settings,
            content = {
                Text(
                    text = Strings.get("core_settings_note"),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )

        // Clusters Info
        clusters.forEach { cluster ->
            ClusterInfoCard(
                cluster = cluster,
                onUpdate = { clusters = DeviceUtils.getCpuClustersInfo() },
                snackbarHostState = snackbarHostState
            )
        }
        
        // Original Low Power Mode Switch
        var isEnabled by remember { mutableStateOf(DeviceUtils.isCoreCtlEnabled(context)) }
        InfoCard(
            title = "corectl",
            icon = Icons.Default.Build,
            content = {
                InfoListItem(
                    headline = Strings.get("corectl_switch"),
                    supporting = Strings.get("corectl_desc"),
                    icon = Icons.Default.Build,
                    trailingContent = {
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { checked ->
                                isEnabled = checked
                                scope.launch {
                                    val result = DeviceUtils.toggleCoreCtl(context, checked)
                                    snackbarHostState.showSnackbar(result)
                                }
                            }
                        )
                    }
                )
            }
        )
    }
}

@Composable
fun CpuCoreCard(core: CpuCoreInfo, modifier: Modifier = Modifier, onToggle: () -> Unit) {
    Card(
        modifier = modifier
            .aspectRatio(0.8f)
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (core.isOnline) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) 
                             else Color.Red.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Cpu${core.id}", style = MaterialTheme.typography.labelSmall)
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(48.dp).padding(4.dp)
            ) {
                CircularProgressIndicator(
                    progress = { core.usage / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = if (core.isOnline) MaterialTheme.colorScheme.primary else Color.Gray,
                    strokeWidth = 3.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = "${core.usage}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    textAlign = TextAlign.Center
                )
            }
            
            Text(
                text = core.currentFreq,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (core.isOnline) MaterialTheme.colorScheme.onSurface else Color.Gray
            )
            Text(
                text = "Temp: ${core.temp}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (core.temp != "N/A") {
                    val t = core.temp.replace("°C", "").toDoubleOrNull() ?: 0.0
                    when {
                        t >= 60 -> Color.Red
                        t >= 50 -> Color(0xFFFFC107)
                        else -> MaterialTheme.colorScheme.primary
                    }
                } else Color.Gray
            )
            Text(
                text = "${core.minFreq}~${core.maxFreq}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ClusterInfoCard(cluster: CpuClusterInfo, onUpdate: () -> Unit, snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showGovernorDialog by remember { mutableStateOf(false) }
    var showFreqDialog by remember { mutableStateOf(false) }
    var freqType by remember { mutableStateOf("min") }
    
    val refCoreId = remember(cluster.cores) { 
        cluster.cores.replace("Cpu", "").split(",","-").first().toInt() 
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = cluster.cores,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ClusterRow(label = Strings.get("cpu_lock_freq"), value = Strings.get("not_set"), onClick = {})
            ClusterRow(
                label = Strings.get("cpu_governor"), 
                value = cluster.governor,
                onClick = { showGovernorDialog = true }
            )
            ClusterRow(
                label = Strings.get("cpu_min_freq"), 
                value = cluster.minFreq,
                onClick = { 
                    freqType = "min"
                    showFreqDialog = true 
                }
            )
            ClusterRow(
                label = Strings.get("cpu_max_freq"), 
                value = cluster.maxFreq,
                onClick = { 
                    freqType = "max"
                    showFreqDialog = true 
                }
            )
            ClusterRow(label = Strings.get("cpu_freq_range"), value = cluster.range, onClick = {})
        }
    }

    if (showGovernorDialog) {
        val governors = DeviceUtils.getAvailableGovernors(refCoreId)
        SelectionDialog(
            title = Strings.get("cpu_governor"),
            options = governors,
            selectedOption = cluster.governor,
            onDismiss = { showGovernorDialog = false },
            onSelect = { selected ->
                scope.launch {
                    val res = DeviceUtils.setCpuGovernor(cluster.cores, selected)
                    snackbarHostState.showSnackbar(res)
                    onUpdate()
                }
                showGovernorDialog = false
            }
        )
    }

    if (showFreqDialog) {
        val freqs = DeviceUtils.getAvailableFrequencies(refCoreId)
        SelectionDialog(
            title = if (freqType == "min") Strings.get("cpu_min_freq") else Strings.get("cpu_max_freq"),
            options = freqs,
            selectedOption = if (freqType == "min") cluster.minFreq else cluster.maxFreq,
            onDismiss = { showFreqDialog = false },
            onSelect = { selected ->
                scope.launch {
                    val res = DeviceUtils.setCpuFreq(cluster.cores, freqType, selected)
                    snackbarHostState.showSnackbar(res)
                    onUpdate()
                }
                showFreqDialog = false
            }
        )
    }
}

@Composable
fun SelectionDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .selectableGroup()
            ) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .selectable(
                                selected = (option == selectedOption),
                                onClick = { onSelect(option) }
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (option == selectedOption),
                            onClick = null
                        )
                        Text(
                            text = option,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(Strings.get("close")) }
        }
    )
}

@Composable
fun ClusterRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyMedium, 
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
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
    val details = remember { DeviceUtils.getHardwareDetails(context) }
    val wifi = remember { DeviceUtils.getWifiInfo(context) }
    val bt = remember { DeviceUtils.getBluetoothInfo(context) }
    val audio = remember { DeviceUtils.getAudioInfo(context) }
    
    InfoCard(
        title = Strings.get("tab_hardware"),
        icon = Icons.Default.Build,
        content = {
            details.entries.forEachIndexed { index, entry ->
                InfoListItem(headline = entry.key, supporting = entry.value, icon = Icons.Default.Info)
                if (index < details.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    )

    InfoCard(
        title = Strings.get("wifi_info"),
        icon = Icons.Default.Settings,
        content = {
            wifi.entries.forEachIndexed { index, entry ->
                InfoListItem(headline = entry.key, supporting = entry.value, icon = Icons.Default.Info)
                if (index < wifi.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    )

    InfoCard(
        title = Strings.get("bt_info"),
        icon = Icons.Default.Phone,
        content = {
            bt.entries.forEachIndexed { index, entry ->
                InfoListItem(headline = entry.key, supporting = entry.value, icon = Icons.Default.Info)
                if (index < bt.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    )

    InfoCard(
        title = Strings.get("audio_info"),
        icon = Icons.Default.Build,
        content = {
            audio.entries.forEachIndexed { index, entry ->
                InfoListItem(headline = entry.key, supporting = entry.value, icon = Icons.Default.Info)
                if (index < audio.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    )

    InfoCard(
        title = "内存与存储",
        icon = Icons.Default.Info,
        content = {
            InfoListItem(headline = Strings.get("mem_total"), supporting = DeviceUtils.formatStorageSize(DeviceUtils.getMemoryInfo(context).totalMem), icon = Icons.Default.Info)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("storage_usage"), supporting = DeviceUtils.formatStorageSize(DeviceUtils.getUsedStorageSize(android.os.Environment.getDataDirectory())), icon = Icons.Default.Info)
        }
    )
}

@Composable
fun ScreenTab(context: Context) {
    val info = DeviceUtils.getScreenInfo(context)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() } // Pager might need its own or shared
    
    var showResDialog by remember { mutableStateOf(false) }
    var showDpiDialog by remember { mutableStateOf(false) }
    var showRefreshDialog by remember { mutableStateOf(false) }

    InfoCard(
        title = Strings.get("tab_screen"),
        icon = Icons.Default.Settings,
        content = {
            InfoListItem(
                headline = Strings.get("screen_res"), 
                supporting = info["resolution"] ?: "N/A", 
                icon = Icons.Default.Info,
                trailingContent = {
                    TextButton(onClick = { showResDialog = true }) {
                        Text(Strings.get("change_res"))
                    }
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(
                headline = Strings.get("screen_refresh"), 
                supporting = info["refresh_rate"] ?: "N/A", 
                icon = Icons.Default.Info,
                trailingContent = {
                    TextButton(onClick = { showRefreshDialog = true }) {
                        Text(Strings.get("change_refresh"))
                    }
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(
                headline = Strings.get("screen_density"), 
                supporting = info["density"] ?: "N/A", 
                icon = Icons.Default.Info,
                trailingContent = {
                    TextButton(onClick = { showDpiDialog = true }) {
                        Text(Strings.get("change_dpi"))
                    }
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("screen_size"), supporting = info["size"] ?: "N/A", icon = Icons.Default.Info)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("screen_xdpi"), supporting = info["xdpi_ydpi"] ?: "N/A", icon = Icons.Default.Info)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("screen_features"), supporting = info["features"] ?: "N/A", icon = Icons.Default.Info)
        }
    )

    InfoCard(
        title = Strings.get("hdr_test"),
        icon = Icons.Default.Visibility,
        content = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = Strings.get("hdr_test_desc"), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Black, Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.White)
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                )
            }
        }
    )

    if (showResDialog) {
        ResolutionInputDialog(
            onDismiss = { showResDialog = false },
            onConfirm = { w, h ->
                scope.launch {
                    DeviceUtils.setScreenSize(w, h)
                    showResDialog = false
                }
            },
            onReset = {
                scope.launch {
                    DeviceUtils.resetScreenSize()
                    showResDialog = false
                }
            }
        )
    }

    if (showDpiDialog) {
        DpiInputDialog(
            onDismiss = { showDpiDialog = false },
            onConfirm = { dpi ->
                scope.launch {
                    DeviceUtils.setScreenDensity(dpi)
                    showDpiDialog = false
                }
            },
            onReset = {
                scope.launch {
                    DeviceUtils.resetScreenDensity()
                    showDpiDialog = false
                }
            }
        )
    }

    if (showRefreshDialog) {
        val rates = DeviceUtils.getAvailableRefreshRates(context)
        SelectionDialog(
            title = Strings.get("change_refresh"),
            options = rates,
            selectedOption = info["refresh_rate"] ?: "",
            onDismiss = { showRefreshDialog = false },
            onSelect = { selected ->
                scope.launch {
                    DeviceUtils.setRefreshRate(selected)
                    showRefreshDialog = false
                }
            }
        )
    }
}

@Composable
fun ResolutionInputDialog(onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit, onReset: () -> Unit) {
    var width by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.get("change_res")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = width,
                    onValueChange = { width = it },
                    label = { Text("Width") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text("Height") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                val w = width.toIntOrNull() ?: 0
                val h = height.toIntOrNull() ?: 0
                if (w > 0 && h > 0) onConfirm(w, h)
            }) { Text(Strings.get("apply")) }
        },
        dismissButton = {
            TextButton(onClick = onReset) { Text(Strings.get("reset")) }
        }
    )
}

@Composable
fun DpiInputDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit, onReset: () -> Unit) {
    var dpi by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.get("change_dpi")) },
        text = {
            OutlinedTextField(
                value = dpi,
                onValueChange = { dpi = it },
                label = { Text("DPI") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(onClick = { 
                val d = dpi.toIntOrNull() ?: 0
                if (d > 0) onConfirm(d)
            }) { Text(Strings.get("apply")) }
        },
        dismissButton = {
            TextButton(onClick = onReset) { Text(Strings.get("reset")) }
        }
    )
}

@Composable
fun CameraTab(context: Context) {
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    if (!hasCameraPermission) {
        InfoCard(
            title = Strings.get("tab_camera"),
            icon = Icons.Default.Phone,
            content = {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "需要摄像头权限来显示预览", style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                        Text(text = "授予权限")
                    }
                }
            }
        )
    }

    val cameras = DeviceUtils.getCameraInfo(context)
    cameras.forEach { cam ->
        InfoCard(
            title = "${Strings.get("tab_camera")} ${cam.id} (${cam.facing})",
            icon = Icons.Default.Phone,
            content = {
                if (hasCameraPermission) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp)) {
                        CameraPreview(context, cam.id)
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
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
    var rendererInfo by remember { mutableStateOf("Loading...") }
    var vendorInfo by remember { mutableStateOf("Loading...") }

    InfoCard(
        title = Strings.get("gpu_info"),
        icon = Icons.Default.Build,
        content = {
            InfoListItem(headline = Strings.get("gpu_renderer"), supporting = rendererInfo, icon = Icons.Default.Info)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("gpu_vendor"), supporting = vendorInfo, icon = Icons.Default.Info)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("opengl_version"), supporting = DeviceUtils.getGlEsVersion(context), icon = Icons.Default.Info)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("vulkan_version"), supporting = DeviceUtils.getVulkanVersion(context), icon = Icons.Default.Info)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            InfoListItem(headline = Strings.get("opencl_version"), supporting = DeviceUtils.getOpenCLVersion(), icon = Icons.Default.Info)
        }
    )

    InfoCard(
        title = "OpenGL ES 3D 旋转立方体",
        icon = Icons.Default.Visibility,
        content = {
            Box(modifier = Modifier.fillMaxWidth().height(250.dp).padding(16.dp)) {
                AndroidView(
                    factory = { ctx ->
                        GLSurfaceView(ctx).apply {
                            setEGLContextClientVersion(2)
                            setRenderer(object : GLSurfaceView.Renderer {
                                private var cube: Cube? = null
                                override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
                                    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
                                    GLES20.glEnable(GLES20.GL_DEPTH_TEST)
                                    cube = Cube()
                                    rendererInfo = GLES20.glGetString(GLES20.GL_RENDERER) ?: "Unknown"
                                    vendorInfo = GLES20.glGetString(GLES20.GL_VENDOR) ?: "Unknown"
                                    
                                    // Update BroadcastManager for PC version
                                    BroadcastManager.gpuRenderer = rendererInfo
                                    BroadcastManager.gpuVendor = vendorInfo
                                }
                                override fun onDrawFrame(unused: GL10) {
                                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
                                    val vPMatrix = FloatArray(16)
                                    val projectionMatrix = FloatArray(16)
                                    val viewMatrix = FloatArray(16)
                                    val rotationMatrix = FloatArray(16)
                                    val scratch = FloatArray(16)
                                    Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -5f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
                                    Matrix.perspectiveM(projectionMatrix, 0, 45f, 1f, 3f, 7f)
                                    Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
                                    val time = (android.os.SystemClock.uptimeMillis() % 4000L).toFloat() / 4000L * 360f
                                    Matrix.setRotateM(rotationMatrix, 0, time, 1.0f, 1.0f, 1.0f)
                                    Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0)
                                    cube?.draw(scratch)
                                }
                                override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
                                    GLES20.glViewport(0, 0, width, height)
                                }
                            })
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    )

    InfoCard(
        title = "Vulkan 3D 渲染演示 (模拟)",
        icon = Icons.Default.Visibility,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(16.dp)
                    .background(Color.Black, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                if (DeviceUtils.getVulkanVersion(context) != "不支持") {
                    VulkanWireframeCube()
                } else {
                    Text("当前硬件不支持 Vulkan 渲染", color = Color.White)
                }
            }
        }
    )
}

@Composable
fun VulkanWireframeCube() {
    var rotationX by remember { mutableStateOf(0f) }
    var rotationY by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            rotationX += 1.5f
            rotationY += 2f
            delay(16)
        }
    }

    Canvas(modifier = Modifier.size(150.dp)) {
        val size = size.minDimension / 2.5f
        val points = listOf(
            floatArrayOf(-1f, -1f, -1f), floatArrayOf(1f, -1f, -1f),
            floatArrayOf(1f, 1f, -1f), floatArrayOf(-1f, 1f, -1f),
            floatArrayOf(-1f, -1f, 1f), floatArrayOf(1f, -1f, 1f),
            floatArrayOf(1f, 1f, 1f), floatArrayOf(-1f, 1f, 1f)
        ).map { p ->
            // Rotate X
            val y1 = p[1] * Math.cos(Math.toRadians(rotationX.toDouble())) - p[2] * Math.sin(Math.toRadians(rotationX.toDouble()))
            val z1 = p[1] * Math.sin(Math.toRadians(rotationX.toDouble())) + p[2] * Math.cos(Math.toRadians(rotationX.toDouble()))
            // Rotate Y
            val x2 = p[0] * Math.cos(Math.toRadians(rotationY.toDouble())) + z1 * Math.sin(Math.toRadians(rotationY.toDouble()))
            val z2 = -p[0] * Math.sin(Math.toRadians(rotationY.toDouble())) + z1 * Math.cos(Math.toRadians(rotationY.toDouble()))
            // Project
            val factor = 1.5f / (2f - (z2.toFloat() / 2f))
            androidx.compose.ui.geometry.Offset(
                center.x + x2.toFloat() * size * factor,
                center.y + y1.toFloat() * size * factor
            )
        }

        val edges = listOf(
            0 to 1, 1 to 2, 2 to 3, 3 to 0,
            4 to 5, 5 to 6, 6 to 7, 7 to 4,
            0 to 4, 1 to 5, 2 to 6, 3 to 7
        )

        edges.forEach { (start, end) ->
            drawLine(
                color = Color(0xFF00E5FF),
                start = points[start],
                end = points[end],
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun OpenGLRenderView() {
    AndroidView(
        factory = { ctx ->
            GLSurfaceView(ctx).apply {
                setEGLContextClientVersion(2)
                setRenderer(MyGLRenderer())
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

class MyGLRenderer : GLSurfaceView.Renderer {
    private var cube: Cube? = null

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        cube = Cube()
    }

    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)

    override fun onDrawFrame(unused: GL10) {
        val scratch = FloatArray(16)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -5f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        val time = (android.os.SystemClock.uptimeMillis() % 4000L).toFloat() / 4000L * 360f
        Matrix.setRotateM(rotationMatrix, 0, time, 1.0f, 1.0f, 1.0f)
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0)

        cube?.draw(scratch)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio: Float = width.toFloat() / height
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }
}

class Cube {
    private val vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
        "attribute vec4 vPosition;" +
        "attribute vec4 vColor;" +
        "varying vec4 vVaryingColor;" +
        "void main() {" +
        "  gl_Position = uMVPMatrix * vPosition;" +
        "  vVaryingColor = vColor;" +
        "}"

    private val fragmentShaderCode =
        "precision mediump float;" +
        "varying vec4 vVaryingColor;" +
        "void main() {" +
        "  gl_FragColor = vVaryingColor;" +
        "}"

    private var program: Int = 0
    private var vertexBuffer: FloatBuffer? = null
    private var colorBuffer: FloatBuffer? = null
    private var drawListBuffer: java.nio.ShortBuffer? = null

    private val cubeCoords = floatArrayOf(
        -0.5f,  0.5f,  0.5f,   -0.5f, -0.5f,  0.5f,    0.5f, -0.5f,  0.5f,    0.5f,  0.5f,  0.5f,
        -0.5f,  0.5f, -0.5f,   -0.5f, -0.5f, -0.5f,    0.5f, -0.5f, -0.5f,    0.5f,  0.5f, -0.5f
    )

    private val colors = floatArrayOf(
        1f, 0f, 0f, 1f,  0f, 1f, 0f, 1f,  0f, 0f, 1f, 1f,  1f, 1f, 0f, 1f,
        0f, 1f, 1f, 1f,  1f, 0f, 1f, 1f,  1f, 1f, 1f, 1f,  0.5f, 0.5f, 0.5f, 1f
    )

    private val drawOrder = shortArrayOf(
        0, 1, 2, 0, 2, 3, 4, 5, 6, 4, 6, 7, 0, 1, 5, 0, 5, 4,
        2, 3, 7, 2, 7, 6, 0, 3, 7, 0, 7, 4, 1, 2, 6, 1, 6, 5
    )

    init {
        val bb = ByteBuffer.allocateDirect(cubeCoords.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer().apply { put(cubeCoords); position(0) }

        val cb = ByteBuffer.allocateDirect(colors.size * 4)
        cb.order(ByteOrder.nativeOrder())
        colorBuffer = cb.asFloatBuffer().apply { put(colors); position(0) }

        val dlb = ByteBuffer.allocateDirect(drawOrder.size * 2)
        dlb.order(ByteOrder.nativeOrder())
        drawListBuffer = dlb.asShortBuffer().apply { put(drawOrder); position(0) }

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().apply {
            GLES20.glAttachShader(this, vertexShader)
            GLES20.glAttachShader(this, fragmentShader)
            GLES20.glLinkProgram(this)
        }
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(program)
        val matrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, mvpMatrix, 0)

        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)

        val colorHandle = GLES20.glGetAttribLocation(program, "vColor")
        GLES20.glEnableVertexAttribArray(colorHandle)
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 16, colorBuffer)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.size, GLES20.GL_UNSIGNED_SHORT, drawListBuffer)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also {
            GLES20.glShaderSource(it, shaderCode)
            GLES20.glCompileShader(it)
        }
    }
}

@Composable
fun SensorsTab(context: Context) {
    val sensors = remember { DeviceUtils.getSensors(context) }
    var selectedSensorForData by remember { mutableStateOf<SensorInfo?>(null) }

    TemperatureSensorsCard(context)

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
fun PartitionsTab(context: Context, snackbarHostState: SnackbarHostState) {
    val partitions = remember { DeviceUtils.getPartitions() }
    val scope = rememberCoroutineScope()
    
    var selectedPartition by remember { mutableStateOf<PartitionInfo?>(null) }
    var operationType by remember { mutableStateOf("") } // "flash" or "export"
    
    val flashLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && selectedPartition != null) {
            scope.launch {
                val res = DeviceUtils.flashPartition(context, selectedPartition!!.path, uri)
                snackbarHostState.showSnackbar(res)
            }
        }
    }
    
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null && selectedPartition != null) {
            scope.launch {
                val res = DeviceUtils.exportPartition(context, selectedPartition!!.path, uri)
                snackbarHostState.showSnackbar(res)
            }
        }
    }

    InfoCard(
        title = Strings.get("tab_partitions") + " (${partitions.size})",
        icon = Icons.Default.Settings,
        content = {
            partitions.forEachIndexed { index, part ->
                PartitionListItem(
                    partition = part,
                    onFlash = {
                        selectedPartition = part
                        operationType = "flash"
                    },
                    onExport = {
                        selectedPartition = part
                        operationType = "export"
                    }
                )
                if (index < partitions.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    )
    
    if (selectedPartition != null) {
        AlertDialog(
            onDismissRequest = { selectedPartition = null },
            title = { Text(if (operationType == "flash") Strings.get("flash") else Strings.get("export")) },
            text = { Text("${if (operationType == "flash") Strings.get("confirm_flash") else Strings.get("confirm_export")}\n\n${selectedPartition!!.name} (${DeviceUtils.formatStorageSize(selectedPartition!!.size)})") },
            confirmButton = {
                Button(
                    onClick = {
                        val part = selectedPartition!!
                        if (operationType == "flash") {
                            flashLauncher.launch("*/*")
                        } else {
                            exportLauncher.launch("${part.name}.img")
                        }
                        selectedPartition = null
                    },
                    colors = if (operationType == "flash") ButtonDefaults.buttonColors(containerColor = Color.Red) else ButtonDefaults.buttonColors()
                ) {
                    Text(Strings.get("apply"))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPartition = null }) {
                    Text(Strings.get("close"))
                }
            }
        )
    }
}

@Composable
fun PartitionListItem(partition: PartitionInfo, onFlash: () -> Unit, onExport: () -> Unit) {
    ListItem(
        headlineContent = { Text(partition.name, fontWeight = FontWeight.Bold) },
        supportingContent = {
            Column {
                Text("${Strings.get("partition_size")}: ${DeviceUtils.formatStorageSize(partition.size)}")
                Text("${Strings.get("partition_path")}: ${partition.path}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        trailingContent = {
            Row {
                TextButton(onClick = onExport) {
                    Text(Strings.get("export"))
                }
                TextButton(onClick = onFlash, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
                    Text(Strings.get("flash"))
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
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
                        val formattedValue = String.format("%.3f", value)
                        Text(text = "Value[$index]: $formattedValue", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    )
}

@Composable
fun TemperatureSensorsCard(context: Context) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val tempSensors = sensorManager.getSensorList(Sensor.TYPE_ALL).filter { 
        it.type == Sensor.TYPE_AMBIENT_TEMPERATURE || it.type == Sensor.TYPE_TEMPERATURE || it.name.lowercase().contains("temp")
    }
    
    var thermalZones by remember { mutableStateOf(DeviceUtils.getThermalInfo()) }
    LaunchedEffect(Unit) {
        while(true) {
            thermalZones = DeviceUtils.getThermalInfo()
            delay(2000)
        }
    }

    if (tempSensors.isNotEmpty() || thermalZones.isNotEmpty()) {
        InfoCard(
            title = Strings.get("temp_sensors"),
            icon = Icons.Default.Settings,
            content = {
                // Hardware Thermal Zones (CPU, GPU, etc)
                thermalZones.forEachIndexed { index, zone ->
                    val temp = zone.second.replace(" °C", "").toDoubleOrNull() ?: 0.0
                    val color = when {
                        temp >= 60 -> Color.Red
                        temp >= 50 -> Color(0xFFFFC107) // Amber/Yellow
                        else -> MaterialTheme.colorScheme.primary
                    }
                    InfoListItem(
                        headline = zone.first, 
                        supporting = zone.second, 
                        icon = Icons.Default.Info,
                        iconTint = color
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                // Standard Android Sensors
                tempSensors.forEachIndexed { index, sensor ->
                    var tempValue by remember { mutableStateOf("Waiting...") }
                    var tintColor by remember { mutableStateOf(Color.Unspecified) }
                    
                    DisposableEffect(sensor) {
                        val listener = object : SensorEventListener {
                            override fun onSensorChanged(event: SensorEvent) {
                                val value = event.values[0]
                                tempValue = "$value °C"
                                tintColor = when {
                                    value >= 60 -> Color.Red
                                    value >= 50 -> Color(0xFFFFC107)
                                    else -> Color.Unspecified
                                }
                            }
                            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                        }
                        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
                        onDispose { sensorManager.unregisterListener(listener) }
                    }
                    InfoListItem(
                        headline = sensor.name, 
                        supporting = tempValue, 
                        icon = Icons.Default.Info,
                        iconTint = if (tintColor == Color.Unspecified) MaterialTheme.colorScheme.primary else tintColor
                    )
                    if (index < tempSensors.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        )
    }
}

@Composable
fun CameraPreview(context: Context, cameraId: String) {
    AndroidView(
        factory = { ctx ->
            TextureView(ctx).apply {
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                        openCamera(context, cameraId, surface)
                    }
                    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
                    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
                    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

private fun openCamera(context: Context, cameraId: String, surfaceTexture: SurfaceTexture) {
    val manager = context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
    try {
        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                val surface = Surface(surfaceTexture)
                val builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                builder.addTarget(surface)
                camera.createCaptureSession(listOf(surface), object : android.hardware.camera2.CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: android.hardware.camera2.CameraCaptureSession) {
                        session.setRepeatingRequest(builder.build(), null, null)
                    }
                    override fun onConfigureFailed(session: android.hardware.camera2.CameraCaptureSession) {}
                }, null)
            }
            override fun onDisconnected(camera: CameraDevice) { camera.close() }
            override fun onError(camera: CameraDevice, error: Int) { camera.close() }
        }, null)
    } catch (e: SecurityException) {
        // Handle permission error
    } catch (e: Exception) {
        e.printStackTrace()
    }
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
