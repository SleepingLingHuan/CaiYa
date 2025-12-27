package com.example.jjsj.data.model

/**
 * 持仓收益数据（某一天的收益）
 * @property date 日期（yyyy-MM-dd）
 * @property revenue 当日收益
 */
data class PositionRevenueHistory(
    val date: String,
    val revenue: Double  // 当日收益（可正可负）
)
