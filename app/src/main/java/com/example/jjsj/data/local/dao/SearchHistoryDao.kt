package com.example.jjsj.data.local.dao

import androidx.room.*
import com.example.jjsj.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 搜索历史DAO
 */
@Dao
interface SearchHistoryDao {
    
    /**
     * 获取所有搜索历史（按时间降序）
     */
    @Query("SELECT * FROM search_history ORDER BY searchTime DESC LIMIT 20")
    fun getAllHistory(): Flow<List<SearchHistoryEntity>>
    
    /**
     * 插入或更新搜索历史
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: SearchHistoryEntity)
    
    /**
     * 删除指定的搜索历史
     */
    @Delete
    suspend fun delete(history: SearchHistoryEntity)
    
    /**
     * 清空所有搜索历史
     */
    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}
