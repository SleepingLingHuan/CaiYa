package com.example.jjsj.data.local.dao

import androidx.room.*
import com.example.jjsj.data.local.entity.PositionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 持仓记录DAO
 */
@Dao
interface PositionDao {
    @Query("SELECT * FROM positions ORDER BY createTime DESC")
    fun getAllPositions(): Flow<List<PositionEntity>>
    
    @Query("SELECT * FROM positions ORDER BY createTime DESC")
    suspend fun getAllPositionsSync(): List<PositionEntity>
    
    @Query("SELECT * FROM positions WHERE id = :id")
    suspend fun getPositionById(id: Long): PositionEntity?
    
    @Query("SELECT * FROM positions WHERE fundCode = :fundCode")
    fun getPositionsByFundCode(fundCode: String): Flow<List<PositionEntity>>
    
    @Query("SELECT * FROM positions WHERE fundCode = :fundCode ORDER BY createTime ASC")
    suspend fun getPositionsByFundCodeSync(fundCode: String): List<PositionEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(position: PositionEntity): Long
    
    @Update
    suspend fun update(position: PositionEntity)
    
    @Delete
    suspend fun delete(position: PositionEntity)
    
    @Query("DELETE FROM positions WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM positions")
    suspend fun deleteAll()
}

