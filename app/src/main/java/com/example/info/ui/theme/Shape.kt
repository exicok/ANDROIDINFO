package com.example.info.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material Design 3 形状定义
 * MD3 推荐使用圆角形状，不同尺寸对应不同用途
 */
val Shapes = Shapes(
    // 小形状：用于按钮、输入框等小组件
    small = RoundedCornerShape(8.dp),
    // 中等形状：用于卡片、对话框等
    medium = RoundedCornerShape(12.dp),
    // 大形状：用于底部抽屉、大卡片等
    large = RoundedCornerShape(16.dp),
    // 超大形状：用于全屏对话框等
    extraLarge = RoundedCornerShape(28.dp)
)
