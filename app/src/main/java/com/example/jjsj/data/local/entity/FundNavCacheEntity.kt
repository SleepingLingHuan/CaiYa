package com.example.jjsj.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * 基金净值历史缓存实体
 * 缓存最近60天的净值数据
 */
@Entity(tableName = "fund_nav_cache")
data class FundNavCacheEntity(
    @PrimaryKey
    val fundCode: String,
    val navList: List<FundNavItemCache>,
    val lastUpdateTime: Long // 更新时间戳，24小时后失效
)

@Serializable
data class FundNavItemCache(
    val date: String,           // 净值日期
    val nav: Double,            // 单位净值
    val accumulatedNav: Double, // 累计净值
    val changeRate: Double      // 日增长率
)

