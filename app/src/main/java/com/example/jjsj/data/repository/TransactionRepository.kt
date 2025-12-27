package com.example.jjsj.data.repository

import com.example.jjsj.data.local.dao.FavoriteFundDao
import com.example.jjsj.data.local.dao.FundCacheDao
import com.example.jjsj.data.local.dao.PositionDao
import com.example.jjsj.data.local.dao.TransactionDao
import com.example.jjsj.data.local.entity.TransactionEntity
import com.example.jjsj.data.model.Transaction
import com.example.jjsj.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * 交易记录数据仓库
 */
class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val favoriteFundDao: FavoriteFundDao,
    private val positionDao: PositionDao,
    private val fundCacheDao: FundCacheDao
) {
    
    /**
     * 获取所有交易记录
     */
    fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toTransaction() }
        }
    }
    
    /**
     * 根据基金代码获取交易记录
     */
    fun getTransactionsByFundCode(fundCode: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByFundCode(fundCode).map { entities ->
            entities.map { it.toTransaction() }
        }
    }
    
    /**
     * 添加交易记录
     */
    suspend fun addTransaction(transaction: Transaction): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val entity = transaction.toEntity()
                transactionDao.insert(entity)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 清除所有数据（测试用）
     * 包括：交易记录、持仓、自选、基金缓存
     */
    suspend fun clearAllData(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                transactionDao.deleteAll()
                positionDao.deleteAll()
                favoriteFundDao.deleteAll()
                fundCacheDao.deleteAll()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

// 扩展函数：TransactionEntity转Transaction
private fun TransactionEntity.toTransaction(): Transaction {
    return Transaction(
        id = id,
        fundCode = fundCode,
        fundName = fundName,
        transactionType = TransactionType.valueOf(transactionType),
        transactionDate = transactionDate,
        amount = amount,
        shares = shares,
        nav = nav,
        createTime = createTime
    )
}

// 扩展函数：Transaction转TransactionEntity
private fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        fundCode = fundCode,
        fundName = fundName,
        transactionType = transactionType.name,
        transactionDate = transactionDate,
        amount = amount,
        shares = shares,
        nav = nav,
        createTime = createTime
    )
}

