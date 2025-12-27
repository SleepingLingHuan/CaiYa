package com.example.jjsj.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 交易记录数据库实体
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fundCode: String,
    val fundName: String,
    val transactionType: String,  // "BUY" or "SELL"
    val transactionDate: String,
    val amount: Double,
    val shares: Double,
    val nav: Double,
    val createTime: Long = System.currentTimeMillis()
)

