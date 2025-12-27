package com.example.jjsj.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 搜索历史实体
 * 记录用户搜索并点击打开的基金
 */
@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey
    val fundCode: String,        // 基金代码
    val fundName: String,         // 基金名称  
    val fundType: String,         // 基金类型
    val searchTime: Long          // 搜索时间（毫秒时间戳）
)
