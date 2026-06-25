package com.example.info.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.info.Strings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.math.pow

private const val BCC_VOLTAGE_0_INDEX = 6
private const val BCC_VOLTAGE_1_INDEX = 11
private const val BCC_CURRENT_INDEX = 8
private const val CURRENT_FULL_IN_MA = 25

class BatteryInfoRepository(private val context: Context) {
    private val batteryManager get() =
        context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    private val dataStore = context.batteryDataStore
    private val lang = context.resources.configuration.locales[0].language

    private val calibFlow = dataStore.data
        .map { prefs ->
            val isMult = prefs[BatteryKeys.MULTIPLY] != false
            val mag    = prefs[BatteryKeys.MULTIPLIER_MAGNITUDE] ?: 0
            if (isMult) 10.0.pow(mag.toDouble()) else 1/10.0.pow(mag.toDouble())
        }

    private val dualBattFlow: Flow<Int> = dataStore.data
        .map { prefs -> if (prefs[BatteryKeys.DUAL_BATTERY] == true) 2 else 1 }

    val isCelsiusFlow: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[BatteryKeys.IS_CELSIUS] ?: true }

    val estimatedFccFlow: Flow<String> = dataStore.data
        .map { prefs ->
            prefs[BatteryKeys.ESTIMATED_FCC]?.toString()
                ?: Strings.getStatic("estimating_fcc", lang)
        }

    val showOplusFlow: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[BatteryKeys.SHOW_OPLUS_FIELDS] ?: true }

    suspend fun getBasicBatteryInfo(): List<BatteryInfo> = withContext(Dispatchers.IO) {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
        val temperature = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -999) ?: 0
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, 0) ?: 0
        val cycleCount = intent?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1) ?: -1
        val isCelsius = isCelsiusFlow.first()

        listOf(
            BatteryInfo(BatteryInfoType.LEVEL, "$level%", false),
            BatteryInfo(BatteryInfoType.STATUS, Strings.getStatic(getStatusString(status), lang), false),
            BatteryInfo(BatteryInfoType.HEALTH, Strings.getStatic(getHealthString(health), lang), false),
            BatteryInfo(BatteryInfoType.CYCLE_COUNT, if (cycleCount != -1) cycleCount.toString() else Strings.getStatic("unknown", lang), false),
            BatteryInfo(BatteryInfoType.TEMP, formatTemperature(temperature, isCelsius), false),
        )
    }

    suspend fun getRootBatteryInfo(): List<BatteryInfo> = withContext(Dispatchers.IO) {
        val calibMultiplier = calibFlow.first()
        val dualBattMultiplier = dualBattFlow.first()
        var rootModeVoltage0 = 0
        var rootModeVoltage1 = 0
        var rootModeCurrent = 0
        var rootModePower = 0.0
        var rootReadFailed = false

        rootModeVoltage0 = safeRootReadInt("bcc_parms", BCC_VOLTAGE_0_INDEX, {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        }) { rootReadFailed = true }

        if (rootModeVoltage0 == 0) {
            rootReadFailed = true
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            rootModeVoltage0 = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
            rootModeCurrent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        } else {
            rootModeVoltage1 = safeRootReadInt("bcc_parms", BCC_VOLTAGE_1_INDEX, { 0 }) { rootReadFailed = true }
            rootModeCurrent = safeRootReadInt("bcc_parms", BCC_CURRENT_INDEX, {
                batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            }) { rootReadFailed = true }
        }

        rootModePower = if (!rootReadFailed) {
            if (isDualBattery()) {
                (rootModeVoltage0 + rootModeVoltage1) * rootModeCurrent * calibMultiplier / 1000000.0
            } else {
                rootModeVoltage0 * rootModeCurrent * calibMultiplier * dualBattMultiplier / 1000000.0
            }
        } else {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            (batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                    * dualBattMultiplier
                    * calibMultiplier
                    * (intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0) / 1000000.0)
        }

        val rm = readBatteryInfo("battery_rm") ?: Strings.getStatic("unknown", lang)
        val fcc = readBatteryInfo("battery_fcc") ?: Strings.getStatic("unknown", lang)
        val soh = readBatteryInfo("battery_soh") ?: Strings.getStatic("unknown", lang)
        val vbatUv = readBatteryInfo("vbat_uv") ?: Strings.getStatic("unknown", lang)
        val sn = readBatteryInfo("battery_sn") ?: Strings.getStatic("unknown", lang)
        val batManDate = readBatteryInfo("battery_manu_date") ?: Strings.getStatic("unknown", lang)
        val battType = readBatteryInfo("battery_type") ?: Strings.getStatic("unknown", lang)
        val designCapacity = readBatteryInfo("design_capacity") ?: Strings.getStatic("unknown", lang)
        
        val coeffList = readTermCoeff(context)
        val rawSoh = calcRawSoh(soh.toIntOrNull() ?: 0, vbatUv.toIntOrNull() ?: 0, coeffList).let {
            if (it < 0.0001f) Strings.getStatic("unknown", lang) else it.toString()
        }
        val rawFcc = calcRawFcc(fcc.toIntOrNull() ?: 0, rawSoh.toFloatOrNull() ?: 0f, vbatUv.toIntOrNull() ?: 0, coeffList, designCapacity.toIntOrNull() ?: 0).let {
            if (it == 0) Strings.getStatic("unknown", lang) else it.toString()
        }
        
        val logMap = readBatteryLogMap()
        val fccInt = fcc.toIntOrNull()
        val qmaxInt = logMap["batt_qmax"]?.toIntOrNull()
        val qMax = qmaxInt?.let { q -> "${normalizeQmax(q, fccInt)} mAh" } ?: Strings.getStatic("unknown", lang)

        listOf(
            BatteryInfo(BatteryInfoType.VOLTAGE, "$rootModeVoltage0 / $rootModeVoltage1 mV", false),
            BatteryInfo(BatteryInfoType.CURRENT, (rootModeCurrent * calibMultiplier).formatWithUnit("mA"), false),
            BatteryInfo(BatteryInfoType.POWER, rootModePower.formatWithUnit("W"), false),
            BatteryInfo(BatteryInfoType.OPLUS_RM, "$rm mAh"),
            BatteryInfo(BatteryInfoType.OPLUS_FCC, "$fcc mAh"),
            BatteryInfo(BatteryInfoType.OPLUS_RAW_FCC, "$rawFcc mAh"),
            BatteryInfo(BatteryInfoType.OPLUS_SOH, "$soh %"),
            BatteryInfo(BatteryInfoType.OPLUS_RAW_SOH, rawSoh.toDoubleOrNull()?.formatWithUnit("%") ?: rawSoh),
            BatteryInfo(BatteryInfoType.OPLUS_QMAX, qMax),
            BatteryInfo(BatteryInfoType.OPLUS_VBAT_UV, "$vbatUv mV"),
            BatteryInfo(BatteryInfoType.OPLUS_SN, sn),
            BatteryInfo(BatteryInfoType.OPLUS_MANU_DATE, batManDate),
            BatteryInfo(BatteryInfoType.OPLUS_BATTERY_TYPE, battType),
            BatteryInfo(BatteryInfoType.OPLUS_DESIGN_CAPACITY, "$designCapacity mAh"),
        )
    }

    suspend fun getNonRootVoltCurrPwr(): List<BatteryInfo> = withContext(Dispatchers.IO) {
        val calibMultiplier = calibFlow.first()
        val dualBattMultiplier = dualBattFlow.first()
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val current = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) * calibMultiplier
        val power = current * voltage * dualBattMultiplier / 1_000_000.0

        listOf(
            BatteryInfo(BatteryInfoType.VOLTAGE, "$voltage mV", false),
            BatteryInfo(BatteryInfoType.CURRENT, current.formatWithUnit("mA"), false),
            BatteryInfo(BatteryInfoType.POWER, power.formatWithUnit("W"), false)
        )
    }

    suspend fun getEstimatedFcc(savedEstimatedFcc: String): BatteryInfo = withContext(Dispatchers.IO) {
        val calibMultiplier = calibFlow.first()
        val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) * calibMultiplier
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        if (kotlin.math.abs(currentNow) <= CURRENT_FULL_IN_MA && batteryLevel == 100) {
            val chargeCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            val fullChargeCapacity = (chargeCounter / (batteryLevel / 100.0)).toInt() / 1000
            if (fullChargeCapacity > 0) {
                // save if needed
            }
        }
        
        if (savedEstimatedFcc != Strings.getStatic("estimating_fcc", lang)) {
            BatteryInfo(BatteryInfoType.EST_FCC, "$savedEstimatedFcc mAh", false)
        } else {
            BatteryInfo(BatteryInfoType.EST_FCC, savedEstimatedFcc, false)
        }
    }

    suspend fun readCustomEntries(): List<BatteryInfo> = coroutineScope {
        val prefs = dataStore.data.first()
        val entriesJson = prefs[BatteryKeys.CUSTOM_ENTRIES] ?: "[]"
        val entries = try { Json.decodeFromString<List<CustomEntry>>(entriesJson) } catch (e: Exception) { emptyList() }
        
        entries.map { entry ->
            async(Dispatchers.IO) {
                val raw = readBatteryInfo("", entry.path) ?: Strings.getStatic("unknown", lang)
                val scaled = raw.toDoubleOrNull()?.let { it * 10.0.pow(entry.scale) }?.let { "%.0f".format(it) } ?: raw
                BatteryInfo(
                    type = BatteryInfoType.CUSTOM,
                    value = buildString {
                        append(scaled)
                        if (entry.unit.isNotBlank()) append(' ').append(entry.unit)
                    },
                    customTitle = entry.title
                )
            }
        }.awaitAll()
    }

    suspend fun getAvailableBatteryInfo(isRoot: Boolean): List<BatteryInfo> {
        val showOplus = showOplusFlow.first()
        return if (isRoot) {
            val infoList = (getBasicBatteryInfo() + getRootBatteryInfo() + readCustomEntries()).toMutableList()
            if (!showOplus) {
                infoList.removeAll { it.type.name.startsWith("OPLUS_") }
            }
            infoList
        } else {
            val infoList = (getBasicBatteryInfo() + getNonRootVoltCurrPwr()).toMutableList()
            val savedFcc = estimatedFccFlow.first()
            infoList.add(getEstimatedFcc(savedFcc))
            infoList
        }
    }
}
