package com.example.jjsj.util

import java.text.DecimalFormat

/**
 * 格式化工具类
 */
object FormatUtils {
    private val percentFormat = DecimalFormat("0.00%")
    private val navFormat = DecimalFormat("0.0000")
    private val amountFormat = DecimalFormat("#,##0.00")
    private val rateFormat = DecimalFormat("+0.00%;-0.00%")
    
    /**
     * 格式化百分比
     */
    fun formatPercent(value: Double): String {
        return percentFormat.format(value / 100)
    }
    
    /**
     * 格式化净值
     */
    fun formatNav(value: Double): String {
        return navFormat.format(value)
    }
    
    /**
     * 格式化金额
     */
    fun formatAmount(value: Double): String {
        return amountFormat.format(value)
    }
    
    /**
     * 格式化涨跌幅（带正负号）
     */
    fun formatRate(value: Double): String {
        return rateFormat.format(value / 100)
    }
    
    /**
     * 获取涨跌颜色提示
     */
    fun getRateColorHint(value: Double): String {
        return when {
            value > 0 -> "up"
            value < 0 -> "down"
            else -> "neutral"
        }
    }
}

