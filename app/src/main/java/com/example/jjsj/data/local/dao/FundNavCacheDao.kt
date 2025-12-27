package com.example.jjsj.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.jjsj.data.local.entity.FundNavCacheEntity

@Dao
interface FundNavCacheDao {
    @Query("SELECT * FROM fund_nav_cache WHERE fundCode = :fundCode")
    suspend fun getNavCache(fundCode: String): FundNavCacheEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNavCache(cache: FundNavCacheEntity)
    
    @Query("DELETE FROM fund_nav_cache WHERE fundCode = :fundCode")
    suspend fun deleteNavCache(fundCode: String)
    
    @Query("DELETE FROM fund_nav_cache WHERE lastUpdateTime < :expireTime")
    suspend fun deleteExpiredCache(expireTime: Long)
}

