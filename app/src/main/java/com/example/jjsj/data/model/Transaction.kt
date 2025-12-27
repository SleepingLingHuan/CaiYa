package com.example.jjsj.data.model

/**
 * 交易记录
 * @property id 记录ID
 * @property fundCode 基金代码
 * @property fundName 基金名称
 * @property transactionType 交易类型：BUY-买入, SELL-卖出
 * @property transactionDate 交易日期
 * @property amount 交易金额
 * @property shares 交易份额
 * @property nav 交易时的净值
 * @property createTime 创建时间戳
 */
data class Transaction(
    val id: Long = 0,
    val fundCode: String,
    val fundName: String,
    val transactionType: TransactionType,
    val transactionDate: String,  // 交易日期（买入日期或卖出日期）
    val amount: Double,            // 交易金额
    val shares: Double,            // 交易份额
    val nav: Double,               // 交易时的净值
    val createTime: Long = System.currentTimeMillis()
)

/**
 * 交易类型
 */
enum class TransactionType {
    BUY,   // 买入
    SELL   // 卖出
}

