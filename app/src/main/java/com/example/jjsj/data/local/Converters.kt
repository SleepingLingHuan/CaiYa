package com.example.jjsj.data.local

import androidx.room.TypeConverter
import com.example.jjsj.data.local.entity.FundNavItemCache
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Room TypeConverters for complex data types
 */
class Converters {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    @TypeConverter
    fun fromFundNavItemCacheList(value: List<FundNavItemCache>): String {
        return json.encodeToString(value)
    }
    
    @TypeConverter
    fun toFundNavItemCacheList(value: String): List<FundNavItemCache> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

