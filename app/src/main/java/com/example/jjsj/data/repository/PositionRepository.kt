package com.example.jjsj.data.repository

import com.example.jjsj.data.local.dao.FundCacheDao
import com.example.jjsj.data.local.dao.PositionDao
import com.example.jjsj.data.local.entity.PositionEntity
import com.example.jjsj.data.model.Position
import com.example.jjsj.data.model.PositionSummary
import com.example.jjsj.data.model.AggregatedPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * 持仓数据仓库
 */
class PositionRepository(
    private val positionDao: PositionDao,
    private val fundCacheDao: FundCacheDao
) {
    
    /**
     * 获取所有持仓
     */
    fun getAllPositions(): Flow<List<Position>> {
        return positionDao.getAllPositions().map { entities ->
            entities.map { entity ->
                // 从缓存获取当前净值
                val currentNav = fundCacheDao.getFundByCode(entity.fundCode)?.nav ?: entity.buyNav
                entity.toPosition(currentNav)
            }
        }
    }
    
    /**
     * 获取聚合持仓列表（同一基金的多次购买合并显示）
     */
    fun getAggregatedPositions(): Flow<List<AggregatedPosition>> {
        return positionDao.getAllPositions().map { entities ->
            // 按基金代码分组
            val groupedByCode = entities.groupBy { it.fundCode }
            
            groupedByCode.mapNotNull { (fundCode, positions) ->
                // 获取当前净值
                val currentNav = fundCacheDao.getFundByCode(fundCode)?.nav 
                    ?: positions.firstOrNull()?.buyNav ?: 0.0
                
                // 计算总份额和总购买金额
                val totalShares = positions.sumOf { it.shares }
                val totalBuyAmount = positions.sumOf { it.buyAmount }
                
                // 检查是否有未确认的净值
                val hasUnconfirmedNav = positions.any { !it.navConfirmed }
                
                // 过滤掉份额接近0且已确认的持仓（精度误差导致的残留）
                // 但保留待确认的持仓，因为它们的份额可能还在计算中
                if (totalShares < 0.01 && !hasUnconfirmedNav) {
                    return@mapNotNull null
                }
                
                // 计算加权平均购买净值
                val averageBuyNav = if (totalShares > 0) {
                    totalBuyAmount / totalShares
                } else {
                    0.0
                }
                
                // 获取日期信息
                val sortedPositions = positions.sortedBy { it.createTime }
                val earliestBuyDate = sortedPositions.firstOrNull()?.buyDate ?: ""
                val latestBuyDate = sortedPositions.lastOrNull()?.buyDate ?: ""
                
                AggregatedPosition(
                    fundCode = fundCode,
                    fundName = positions.firstOrNull()?.fundName ?: "",
                    totalShares = totalShares,
                    averageBuyNav = averageBuyNav,
                    totalBuyAmount = totalBuyAmount,
                    currentNav = currentNav,
                    purchaseCount = positions.size,
                    earliestBuyDate = earliestBuyDate,
                    latestBuyDate = latestBuyDate,
                    hasUnconfirmedNav = hasUnconfirmedNav
                )
            }.sortedByDescending { 
                // 按最近购买时间排序
                entities.filter { it.fundCode == it.fundCode }
                    .maxOfOrNull { it.createTime } ?: 0
            }
        }
    }
    
    /**
     * 获取持仓汇总
     */
    fun getPositionSummary(): Flow<PositionSummary> {
        return getAggregatedPositions().map { aggregatedPositions ->
            val totalCost = aggregatedPositions.sumOf { it.totalCost }
            val totalValue = aggregatedPositions.sumOf { it.currentValue }
            val totalProfit = totalValue - totalCost
            val totalProfitRate = if (totalCost != 0.0) {
                totalProfit / totalCost * 100
            } else {
                0.0
            }
            
            PositionSummary(
                positions = emptyList(), // 不再使用
                totalCost = totalCost,
                totalValue = totalValue,
                totalProfit = totalProfit,
                totalProfitRate = totalProfitRate
            )
        }
    }
    
    /**
     * 根据基金代码获取持仓
     */
    fun getPositionsByFundCode(fundCode: String): Flow<List<Position>> {
        return positionDao.getPositionsByFundCode(fundCode).map { entities ->
            entities.map { entity ->
                val currentNav = fundCacheDao.getFundByCode(entity.fundCode)?.nav ?: entity.buyNav
                entity.toPosition(currentNav)
            }
        }
    }
    
    /**
     * 同步获取所有持仓
     */
    suspend fun getAllPositionsSync(): List<Position> {
        return withContext(Dispatchers.IO) {
            positionDao.getAllPositionsSync().map { entity ->
                val currentNav = fundCacheDao.getFundByCode(entity.fundCode)?.nav ?: entity.buyNav
                entity.toPosition(currentNav)
            }
        }
    }
    
    /**
     * 添加持仓
     */
    suspend fun addPosition(position: Position): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val entity = position.toEntity()
                positionDao.insert(entity)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 更新持仓
     */
    suspend fun updatePosition(position: Position): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val entity = position.toEntity()
                positionDao.update(entity)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 删除持仓
     */
    suspend fun deletePosition(position: Position): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                positionDao.deleteById(position.id)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 清空所有持仓
     */
    suspend fun clearAllPositions(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                positionDao.deleteAll()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 删除某个基金的所有持仓记录
     */
    suspend fun deleteAllPositionsByFundCode(fundCode: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val positions = positionDao.getPositionsByFundCodeSync(fundCode)
                positions.forEach { position ->
                    positionDao.deleteById(position.id)
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 检查是否持有某个基金
     * @param fundCode 基金代码
     * @return 持有的总份额，0表示未持有
     */
    suspend fun getTotalSharesByFundCode(fundCode: String): Double {
        return withContext(Dispatchers.IO) {
            try {
                val positions = positionDao.getPositionsByFundCodeSync(fundCode)
                positions.sumOf { it.shares }
            } catch (e: Exception) {
                0.0
            }
        }
    }
    
    /**
     * 卖出基金
     * @param fundCode 基金代码
     * @param sellAmount 卖出金额
     * @param currentNav 当前净值
     * @return 成功返回卖出份额，失败返回错误信息
     */
    suspend fun sellPosition(
        fundCode: String,
        sellAmount: Double,
        currentNav: Double
    ): Result<Double> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. 获取该基金的所有持仓记录（按购买时间排序，先进先出）
                val positions = positionDao.getPositionsByFundCodeSync(fundCode)
                    .sortedBy { it.createTime }
                
                if (positions.isEmpty()) {
                    return@withContext Result.failure(Exception("未持有该基金"))
                }
                
                // 2. 计算需要卖出的份额
                val sharesToSell = sellAmount / currentNav
                
                // 3. 检查总份额是否足够
                val totalShares = positions.sumOf { it.shares }
                if (sharesToSell > totalShares) {
                    return@withContext Result.failure(
                        Exception("卖出份额(${"%.2f".format(sharesToSell)})超过持有份额(${"%.2f".format(totalShares)})")
                    )
                }
                
                // 4. 按先进先出原则减少份额
                val TOLERANCE = 0.01  // 容差：小于0.01份额视为0
                var remainingSharesToSell = sharesToSell
                for (position in positions) {
                    if (remainingSharesToSell <= TOLERANCE) break
                    
                    if (position.shares <= remainingSharesToSell + TOLERANCE) {
                        // 该持仓全部卖出（包含容差范围内的剩余份额）
                        positionDao.deleteById(position.id)
                        remainingSharesToSell -= position.shares
                    } else {
                        // 部分卖出
                        val remainingShares = position.shares - remainingSharesToSell
                        // 如果剩余份额小于容差，直接删除
                        if (remainingShares < TOLERANCE) {
                            positionDao.deleteById(position.id)
                        } else {
                            val updatedPosition = position.copy(
                                shares = remainingShares,
                                buyAmount = remainingShares * position.buyNav
                            )
                            positionDao.update(updatedPosition)
                        }
                        remainingSharesToSell = 0.0
                    }
                }
                
                Result.success(sharesToSell)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

// 扩展函数：PositionEntity转Position
private fun PositionEntity.toPosition(currentNav: Double): Position {
    return Position(
        id = id,
        fundCode = fundCode,
        fundName = fundName,
        shares = shares,
        buyNav = buyNav,
        buyDate = buyDate,
        buyAmount = buyAmount,
        currentNav = currentNav,
        navConfirmed = navConfirmed
    )
}

// 扩展函数：Position转PositionEntity
private fun Position.toEntity(): PositionEntity {
    return PositionEntity(
        id = id,
        fundCode = fundCode,
        fundName = fundName,
        shares = shares,
        buyNav = buyNav,
        buyDate = buyDate,
        buyAmount = buyAmount,
        navConfirmed = navConfirmed
    )
}

