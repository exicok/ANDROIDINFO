package com.example.info.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.info.Strings
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuFileInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.util.Locale

private const val BCC_CURRENT_INDICES_LAST = 18
private const val OPLUS_CHG_BATTERY_PATH = "/sys/class/oplus_chg/battery/"

fun getStatusString(status: Int): String = when (status) {
    BatteryManager.BATTERY_STATUS_CHARGING -> "charging"
    BatteryManager.BATTERY_STATUS_DISCHARGING -> "discharging"
    BatteryManager.BATTERY_STATUS_FULL -> "full"
    else -> "not_charging"
}

fun getHealthString(health: Int): String = when (health) {
    BatteryManager.BATTERY_HEALTH_GOOD -> "good"
    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "overheat"
    BatteryManager.BATTERY_HEALTH_DEAD -> "dead"
    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "overvolt"
    else -> "unknown"
}

fun Double.formatClean(fractionDigits: Int = 2): String {
    return if (this % 1.0 == 0.0) {
        String.format(Locale.getDefault(), "%d", this.toLong())
    } else {
        String.format(Locale.getDefault(), "%.${fractionDigits}f", this)
    }
}

fun Double.formatWithUnit(unit: String, fractionDigits: Int = 2): String =
    "${this.formatClean(fractionDigits)} $unit"

fun convertCelsiusToFahrenheit(celsius: Double): Double {
    return celsius * 9 / 5 + 32
}

fun formatTemperature(temperature: Int, isCelsius: Boolean): String {
    val tempInCelsius = temperature / 10.0
    return if (isCelsius) {
        String.format(Locale.getDefault(), "%.1f°C", tempInCelsius)
    } else {
        val tempInFahrenheit = convertCelsiusToFahrenheit(tempInCelsius)
        String.format(Locale.getDefault(), "%.1f°F", tempInFahrenheit)
    }
}

suspend fun readBatteryInfo(field: String, basePath: String = OPLUS_CHG_BATTERY_PATH): String? = withContext(Dispatchers.IO) {
    try {
        SuFileInputStream.open(basePath + field).bufferedReader().use { it.readText().trim() }
    } catch (e: IOException) {
        null
    }
}

suspend fun readBatteryInfo(field: String, index: Int): String? = withContext(Dispatchers.IO) {
    try {
        val raw = SuFileInputStream
            .open("$OPLUS_CHG_BATTERY_PATH$field")
            .bufferedReader()
            .use { it.readText().trim() }

        val parts = raw.split(",")
        if (field == "bcc_parms" && parts.size - 1 != BCC_CURRENT_INDICES_LAST) {
            null
        } else {
            parts.getOrNull(index)?.trim()
        }
    } catch (e: IOException) {
        null
    }
}

suspend fun readTermCoeff(context: Context): List<Triple<Int, Int, Int>> = withContext(Dispatchers.IO) {
    val tmpDir = File(context.getExternalFilesDir(null), "read_term_coeff")
    if (!tmpDir.exists()) tmpDir.mkdirs()
    val targetFile = File(tmpDir, "term_coeff")
    val targetPath = targetFile.absolutePath

    val battType = readBatteryInfo("battery_type")
    val primaryPath = "/proc/device-tree/soc/oplus,mms_gauge/$battType/deep_spec,term_coeff"
    val fallbackPath = "/proc/device-tree/soc/oplus,mms_gauge/deep_spec,term_coeff"
    var sourcePath = primaryPath

    val checkPrimary = Shell.cmd("[ -f \"$primaryPath\" ] && echo exists").exec().out.joinToString("").trim()
    if (checkPrimary != "exists") {
        val checkFallback = Shell.cmd("[ -f \"$fallbackPath\" ] && echo exists").exec().out.joinToString("").trim()
        if (checkFallback == "exists") {
            sourcePath = fallbackPath
        } else {
            return@withContext emptyList<Triple<Int, Int, Int>>()
        }
    }

    val ddResult = Shell.cmd("su -c 'dd if=$sourcePath of=$targetPath'").exec()
    if (!ddResult.isSuccess) {
        return@withContext emptyList<Triple<Int, Int, Int>>()
    }

    try {
        val bytes = targetFile.readBytes()
        val buffer = ByteBuffer.wrap(bytes)
        val list = mutableListOf<Triple<Int, Int, Int>>()
        while (buffer.remaining() >= 12) {
            val vbatUv = buffer.int
            val fccOffset = buffer.int
            val sohOffset = buffer.int
            list.add(Triple(vbatUv, fccOffset, sohOffset))
        }
        list
    } catch (e: Exception) {
        emptyList()
    }
}

fun calcRawSoh(
    compensatedSoh: Int,
    vbatUv: Int,
    coeffList: List<Triple<Int, Int, Int>>
): Float {
    if (compensatedSoh == 100) return 0f
    val match = coeffList.find { it.first == vbatUv }
    return if (match != null) {
        val sohOffset = match.third
        val factor = 1 + sohOffset.toFloat() / 100f
        compensatedSoh.toFloat() / factor
    } else {
        compensatedSoh.toFloat()
    }
}

fun calcRawFcc(
    compensatedFcc: Int,
    rawSoh: Float,
    vbatUv: Int,
    coeffList: List<Triple<Int, Int, Int>>,
    designCapacity: Int
): Int {
    if (compensatedFcc == designCapacity) return 0
    if (rawSoh == 0f) return 0
    val match = coeffList.find { it.first == vbatUv }
    return if (match != null) {
        val fccOffset = match.second
        compensatedFcc - (fccOffset * rawSoh.toInt() / 100)
    } else {
        compensatedFcc
    }
}

suspend fun readBatteryLogMap(): Map<String, String> = withContext(Dispatchers.IO) {
    val headLine  = readBatteryInfo("battery_log_head") ?: return@withContext emptyMap<String, String>()
    val valueLine = readBatteryInfo("battery_log_content") ?: return@withContext emptyMap<String, String>()
    val heads  = headLine.split(',')
    val values = valueLine.split(',')
    if (heads.size != values.size) return@withContext emptyMap<String, String>()
    heads.indices
        .filter { it > 0 }
        .associate { idx -> heads[idx] to values[idx] }
}

suspend fun safeRootReadInt(
    path: String,
    index: Int,
    fallback: () -> Int,
    onFallback: () -> Unit
): Int = withContext(Dispatchers.IO) {
    try {
        val valueStr = readBatteryInfo(path, index)
        val parsed = valueStr?.toIntOrNull()
        if (parsed != null) {
            parsed
        } else {
            onFallback()
            fallback()
        }
    } catch (e: Exception) {
        onFallback()
        fallback()
    }
}

suspend fun isDualBattery(): Boolean = withContext(Dispatchers.IO) {
    val line = readBatteryInfo("aging_ffc_data") ?: return@withContext false
    val parts = line.split(',').map { it.trim() }
    if (parts.size > 1) {
        when (parts[1]) {
            "2" -> true
            "1" -> false
            else -> false
        }
    } else false
}

fun normalizeQmax(rawQ: Int, fcc: Int?): Int {
    var q = rawQ
    val ref = fcc ?: 20000
    while (q >= ref * 2) {
        q /= 10
    }
    return q
}
