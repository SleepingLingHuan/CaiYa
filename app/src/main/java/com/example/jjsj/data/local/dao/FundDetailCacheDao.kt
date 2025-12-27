package com.example.jjsj.data.local.dao

import androidx.room.*
import com.example.jjsj.data.local.entity.FundDetailCacheEntity
import kotlinx.coroutines.flow.Flow

/**
 * 基金详情缓存DAO
 */
@Dao
interface FundDetailCacheDao {
    @Query("SELECT * FROM fund_detail_cache WHERE fundCode = :fundCode")
    suspend fun getFundDetailCache(fundCode: String): FundDetailCacheEntity?
    
    @Query("SELECT * FROM fund_detail_cache WHERE fundCode = :fundCode")
    fun getFundDetailCacheFlow(fundCode: String): Flow<FundDetailCacheEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fundDetail: FundDetailCacheEntity)
    
    @Delete
    suspend fun delete(fundDetail: FundDetailCacheEntity)
    
    @Query("DELETE FROM fund_detail_cache WHERE cacheTime < :timestamp")
    suspend fun deleteExpiredCache(timestamp: Long)
    
    @Query("DELETE FROM fund_detail_cache")
    suspend fun deleteAll()
}

