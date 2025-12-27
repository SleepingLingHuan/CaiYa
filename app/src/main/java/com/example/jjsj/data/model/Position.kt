package com.example.jjsj.data.model

/**
 * 持仓记录
 * @property id 记录ID
 * @property fundCode 基金代码
 * @property fundName 基金名称
 * @property shares 持有份额（自动计算：购买金额 / 购买日净值）
 * @property buyNav 购买净值
 * @property buyDate 购买日期
 * @property buyAmount 购买金额
 * @property currentNav 当前净值
 * @property navConfirmed 净值是否已确认
 * @property profit 盈亏金额
 * @property profitRate 盈亏率
 */
data class Position(
    val id: Long = 0,
    val fundCode: String,
    val fundName: String,
    val shares: Double,
    val buyNav: Double,
    val buyDate: String,
    val buyAmount: Double = 0.0,
    val currentNav: Double = 0.0,
    val navConfirmed: Boolean = true,
) {
    // 计算盈亏金额
    val profit: Double
        get() = (currentNav - buyNav) * shares
    
    // 计算盈亏率
    val profitRate: Double
        get() = if (buyNav != 0.0) {
            (currentNav - buyNav) / buyNav * 100
        } else {
            0.0
        }
    
    // 计算当前市值
    val currentValue: Double
        get() = currentNav * shares
    
    // 计算成本金额
    val costValue: Double
        get() = buyNav * shares
}

/**
 * 持仓汇总
 */
data class PositionSummary(
    val positions: List<Position>,
    val totalCost: Double,
    val totalValue: Double,
    val totalProfit: Double,
    val totalProfitRate: Double
)

/**
 * 聚合持仓（同一基金的多次购买合并显示）
 * @property fundCode 基金代码
 * @property fundName 基金名称
 * @property totalShares 总份额（所有购买记录的份额之和）
 * @property averageBuyNav 平均购买净值（加权平均）
 * @property totalBuyAmount 总购买金额
 * @property currentNav 当前净值
 * @property purchaseCount 购买次数
 * @property earliestBuyDate 最早购买日期
 * @property latestBuyDate 最近购买日期
 * @property hasUnconfirmedNav 是否有未确认的净值
 */
data class AggregatedPosition(
    val fundCode: String,
    val fundName: String,
    val totalShares: Double,          // 总份额
    val averageBuyNav: Double,         // 平均买入净值（加权平均）
    val totalBuyAmount: Double,        // 总购买金额
    val currentNav: Double,            // 当前净值
    val purchaseCount: Int,            // 购买次数
    val earliestBuyDate: String,       // 最早购买日期
    val latestBuyDate: String,         // 最近购买日期
    val hasUnconfirmedNav: Boolean = false  // 是否有未确认的净值
) {
    // 计算总成本
    val totalCost: Double
        get() = totalBuyAmount
    
    // 计算当前市值（如果有未确认的净值，按成本计算）
    val currentValue: Double
        get() = if (hasUnconfirmedNav) totalCost else currentNav * totalShares
    
    // 计算总盈亏（如果有未确认的净值，返回0）
    val totalProfit: Double
        get() = if (hasUnconfirmedNav) 0.0 else currentValue - totalCost
    
    // 计算盈亏率（如果有未确认的净值，返回0）
    val profitRate: Double
        get() = if (hasUnconfirmedNav) {
            0.0
        } else if (totalCost != 0.0) {
            totalProfit / totalCost * 100
        } else {
            0.0
        }
}

