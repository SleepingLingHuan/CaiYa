package com.example.jjsj.data.model

import kotlinx.serialization.Serializable

/**
 * 基金实体类
 * @property code 基金代码
 * @property name 基金名称
 * @property type 基金类型（股票型、债券型、混合型等）
 * @property nav 最新净值
 * @property accumulatedNav 累计净值
 * @property changeRate 涨跌幅（百分比）
 * @property changeAmount 涨跌金额
 * @property manager 基金经理
 * @property company 基金公司
 * @property scale 基金规模（亿元）
 * @property establishDate 成立日期
 */
@Serializable
data class Fund(
    val code: String,
    val name: String,
    val type: String,
    val nav: Double,
    val accumulatedNav: Double,
    val changeRate: Double,
    val changeAmount: Double,
    val manager: String,
    val company: String,
    val scale: Double,
    val establishDate: String
)

/**
 * 基金详细信息
 */
@Serializable
data class FundDetail(
    val fund: Fund,
    val description: String,
    val investmentStrategy: String,
    val performanceData: PerformanceData,
    val topHoldings: List<Holding>
)

/**
 * 基金业绩数据
 */
@Serializable
data class PerformanceData(
    val week: Double,
    val month: Double,
    val threeMonths: Double,
    val sixMonths: Double,
    val year: Double,
    val threeYears: Double,
    val allTime: Double
)

/**
 * 基金持仓（重仓股）
 */
@Serializable
data class Holding(
    val code: String,
    val name: String,
    val percentage: Double,
    val shares: Long,
    val marketValue: Double
)

