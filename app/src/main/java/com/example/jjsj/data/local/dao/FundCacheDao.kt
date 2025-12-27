package com.example.jjsj.data.local.dao

import androidx.room.*
import com.example.jjsj.data.local.entity.FundCacheEntity
import kotlinx.coroutines.flow.Flow

/**
 * 基金缓存DAO
 */
@Dao
interface FundCacheDao {
    @Query("SELECT * FROM fund_cache ORDER BY updateTime DESC")
    fun getAllCachedFunds(): Flow<List<FundCacheEntity>>
    
    @Query("SELECT * FROM fund_cache WHERE code = :code")
    suspend fun getFundByCode(code: String): FundCacheEntity?
    
    @Query("SELECT * FROM fund_cache WHERE name LIKE '%' || :keyword || '%' OR code LIKE '%' || :keyword || '%'")
    suspend fun searchFunds(keyword: String): List<FundCacheEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fund: FundCacheEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(funds: List<FundCacheEntity>)
    
    @Delete
    suspend fun delete(fund: FundCacheEntity)
    
    @Query("DELETE FROM fund_cache WHERE updateTime < :timestamp")
    suspend fun deleteOldCache(timestamp: Long)
    
    @Query("DELETE FROM fund_cache")
    suspend fun deleteAll()
}

