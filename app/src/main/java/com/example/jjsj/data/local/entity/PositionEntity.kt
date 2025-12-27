package com.example.jjsj.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 持仓记录数据库实体
 */
@Entity(tableName = "positions")
data class PositionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fundCode: String,
    val fundName: String,
    val shares: Double,         // 持有份额（根据购买金额和购买日净值自动计算）
    val buyNav: Double,          // 购买日净值
    val buyDate: String,         // 购买日期 (yyyy-MM-dd)
    val buyAmount: Double = 0.0, // 购买金额（新增字段）
    val navConfirmed: Boolean = true, // 净值是否已确认（默认为true，当日购买时为false）
    val createTime: Long = System.currentTimeMillis()
)

