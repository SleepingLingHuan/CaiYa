package com.example.jjsj.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 自选基金数据库实体
 */
@Entity(tableName = "favorite_funds")
data class FavoriteFundEntity(
    @PrimaryKey
    val fundCode: String,
    val fundName: String,
    val addTime: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
)

