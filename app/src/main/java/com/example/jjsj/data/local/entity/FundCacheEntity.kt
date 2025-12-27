package com.example.jjsj.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 基金数据缓存实体
 */
@Entity(tableName = "fund_cache")
data class FundCacheEntity(
    @PrimaryKey
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
    val establishDate: String,
    val updateTime: Long = System.currentTimeMillis()
)

