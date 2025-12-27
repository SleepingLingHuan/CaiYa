package com.example.jjsj.data.local.dao

import androidx.room.*
import com.example.jjsj.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 交易记录DAO
 */
@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY createTime DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE fundCode = :fundCode ORDER BY createTime DESC")
    fun getTransactionsByFundCode(fundCode: String): Flow<List<TransactionEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long
    
    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int
}

