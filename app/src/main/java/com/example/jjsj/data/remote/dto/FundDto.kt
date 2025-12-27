package com.example.jjsj.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 基金API响应数据传输对象
 */
@Serializable
data class FundListResponse(
    @SerialName("data")
    val data: List<FundDto>,
    @SerialName("total")
    val total: Int
)

@Serializable
data class FundDto(
    @SerialName("code")
    val code: String,
    @SerialName("name")
    val name: String,
    @SerialName("type")
    val type: String,
    @SerialName("nav")
    val nav: Double,
    @SerialName("accumulated_nav")
    val accumulatedNav: Double,
    @SerialName("change_rate")
    val changeRate: Double,
    @SerialName("change_amount")
    val changeAmount: Double,
    @SerialName("manager")
    val manager: String = "",
    @SerialName("company")
    val company: String = "",
    @SerialName("scale")
    val scale: Double = 0.0,
    @SerialName("establish_date")
    val establishDate: String = ""
)

@Serializable
data class FundDetailResponse(
    @SerialName("data")
    val data: FundDetailDto
)

@Serializable
data class FundDetailDto(
    @SerialName("fund")
    val fund: FundDto,
    @SerialName("description")
    val description: String = "",
    @SerialName("investment_strategy")
    val investmentStrategy: String = "",
    @SerialName("performance")
    val performance: PerformanceDto,
    @SerialName("top_holdings")
    val topHoldings: List<HoldingDto> = emptyList()
)

@Serializable
data class PerformanceDto(
    @SerialName("week")
    val week: Double = 0.0,
    @SerialName("month")
    val month: Double = 0.0,
    @SerialName("three_months")
    val threeMonths: Double = 0.0,
    @SerialName("six_months")
    val sixMonths: Double = 0.0,
    @SerialName("year")
    val year: Double = 0.0,
    @SerialName("three_years")
    val threeYears: Double = 0.0,
    @SerialName("all_time")
    val allTime: Double = 0.0
)

@Serializable
data class HoldingDto(
    @SerialName("code")
    val code: String,
    @SerialName("name")
    val name: String,
    @SerialName("percentage")
    val percentage: Double,
    @SerialName("shares")
    val shares: Long = 0L,
    @SerialName("market_value")
    val marketValue: Double = 0.0
)

