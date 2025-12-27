package com.example.jjsj.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * 基金详情缓存实体
 * 用于缓存从东方财富网站抓取的基金详细信息
 * 缓存有效期：60分钟
 */
@Entity(tableName = "fund_detail_cache")
data class FundDetailCacheEntity(
    @PrimaryKey
    val fundCode: String,
    
    // 基本信息
    val fundName: String,
    val fundFullName: String = "",
    val fundType: String = "",
    val establishDate: String = "",
    val establishScale: String = "",
    val assetScale: String = "",
    val shareScale: String = "",
    
    // 管理信息
    val fundManager: String = "",
    val fundCompany: String = "",
    val fundCustodian: String = "",  // 托管人
    
    // 费率信息
    val managementFeeRate: String = "",  // 管理费率
    val custodianFeeRate: String = "",   // 托管费率
    val maxSubscriptionFee: String = "", // 最高认购费率
    val maxPurchaseFee: String = "",     // 最高申购费率
    val maxRedemptionFee: String = "",   // 最高赎回费率
    
    // 投资信息
    val investmentObjective: String = "",     // 投资目标
    val investmentPhilosophy: String = "",   // 投资理念
    val investmentScope: String = "",        // 投资范围
    val investmentStrategy: String = "",     // 投资策略
    val dividendPolicy: String = "",         // 分红政策
    val riskReturnCharacter: String = "",    // 风险收益特征
    
    // 业绩基准
    val performanceBenchmark: String = "",
    
    // 累计分红
    val accumulatedDividend: String = "",
    
    // 持仓信息（JSON格式存储）
    val topHoldingsJson: String = "[]",  // 前十大持仓，JSON数组
    
    // 缓存时间
    val cacheTime: Long = System.currentTimeMillis(),
    val updateTime: Long = System.currentTimeMillis()
) {
    /**
     * 检查缓存是否过期（60分钟）
     */
    fun isExpired(): Boolean {
        val sixtyMinutes = 60L * 60 * 1000  // 60分钟的毫秒数
        return System.currentTimeMillis() - cacheTime > sixtyMinutes
    }
    
    /**
     * 解析持仓股JSON为列表
     */
    fun getTopHoldings(): List<StockHolding> {
        return try {
            if (topHoldingsJson.isBlank() || topHoldingsJson == "[]") {
                emptyList()
            } else {
                kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                    .decodeFromString(topHoldingsJson)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

/**
 * 持仓信息数据类
 */
@Serializable
data class StockHolding(
    val stockCode: String,      // 股票代码
    val stockName: String,       // 股票名称
    val holdingRatio: String,    // 持仓占比
    val shares: String = "",     // 持股数（万股）
    val marketValue: String = "" // 持仓市值（万元）
)

