package com.example.jjsj.data.model

/**
 * 基金净值数据模型
 */
data class FundNav(
    val date: String,           // 净值日期 2025-10-13
    val nav: Double,            // 单位净值 1.0970
    val accumulatedNav: Double, // 累计净值 3.6700  
    val changeRate: Double      // 日增长率 -0.09
)

