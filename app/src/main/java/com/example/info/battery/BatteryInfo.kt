package com.example.info.battery

data class BatteryInfo(
    val type: BatteryInfoType,
    val value: String,
    val isOplus: Boolean = true,
    val customTitle: String? = null
)
