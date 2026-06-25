package com.example.info.battery

enum class BatteryInfoType(
    val key: String,
    val stringKey: String
) {
    LEVEL                    ("Level",                     "battery_level"),
    TEMP                     ("Temperature",               "battery_temp"),
    STATUS                   ("Status",                    "battery_status"),
    HEALTH                   ("Health",                    "battery_health_status"),
    CYCLE_COUNT              ("Cycle Count",               "battery_cycle_count"),
    VOLTAGE                  ("Voltage",                   "battery_voltage"),
    CURRENT                  ("Current",                   "battery_current"),
    POWER                    ("Power",                     "battery_power"),
    EST_FCC                  ("Estimated FCC",             "estimated_fcc"),
    OPLUS_RM                 ("battery_rm",                "remaining_charge_counter"),
    OPLUS_FCC                ("battery_fcc",               "full_charge_capacity_battery_fcc"),
    OPLUS_RAW_FCC            ("battery_fcc (raw)",         "raw_full_charge_capacity"),
    OPLUS_SOH                ("battery_soh",               "battery_health_battery_soh"),
    OPLUS_RAW_SOH            ("battery_soh (raw)",         "raw_battery_health"),
    OPLUS_QMAX               ("batt_qmax",                 "battery_qmax"),
    OPLUS_VBAT_UV            ("vbat_uv",                   "battery_under_voltage_threshold"),
    OPLUS_SN                 ("battery_sn",                "battery_serial_number"),
    OPLUS_MANU_DATE          ("battery_manu_date",         "battery_manufacture_date"),
    OPLUS_BATTERY_TYPE       ("battery_type",              "battery_type_sysfs"),
    OPLUS_DESIGN_CAPACITY    ("design_capacity",           "battery_capacity"),
    CUSTOM                   ("custom",                    "custom_battery_info")
}
