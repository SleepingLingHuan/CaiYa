package com.example.jjsj.data.local.dao

import androidx.room.*
import com.example.jjsj.data.local.entity.FavoriteFundEntity
import kotlinx.coroutines.flow.Flow

/**
 * 自选基金DAO
 */
@Dao
interface FavoriteFundDao {
    @Query("SELECT * FROM favorite_funds ORDER BY sortOrder ASC, addTime DESC")
    fun getAllFavorites(): Flow<List<FavoriteFundEntity>>
    
    @Query("SELECT * FROM favorite_funds WHERE fundCode = :fundCode")
    suspend fun getFavoriteByCode(fundCode: String): FavoriteFundEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fund: FavoriteFundEntity)
    
    @Delete
    suspend fun delete(fund: FavoriteFundEntity)
    
    @Query("DELETE FROM favorite_funds WHERE fundCode = :fundCode")
    suspend fun deleteByCode(fundCode: String)
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_funds WHERE fundCode = :fundCode)")
    suspend fun isFavorite(fundCode: String): Boolean
    
    @Query("DELETE FROM favorite_funds")
    suspend fun deleteAll()
}

