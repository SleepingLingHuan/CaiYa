package com.example.jjsj.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jjsj.data.model.Position
import com.example.jjsj.data.model.PositionSummary
import com.example.jjsj.data.model.AggregatedPosition
import com.example.jjsj.data.repository.PositionRepository
import com.example.jjsj.data.repository.FundRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.round

/**
 * 持仓ViewModel
 */
class PositionViewModel(
    private val repository: PositionRepository,
    private val fundRepository: FundRepository
) : ViewModel() {
    
    // 聚合持仓列表（同一基金合并显示）
    val aggregatedPositions: StateFlow<List<AggregatedPosition>> = repository.getAggregatedPositions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 持仓汇总
    val positionSummary: StateFlow<PositionSummary> = repository.getPositionSummary()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PositionSummary(
                positions = emptyList(),
                totalCost = 0.0,
                totalValue = 0.0,
                totalProfit = 0.0,
                totalProfitRate = 0.0
            )
        )
    
    // 操作状态
    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()
    
    // 基金名称状态（用于自动获取）
    private val _fundNameState = MutableStateFlow<String?>("")
    val fundNameState: StateFlow<String?> = _fundNameState.asStateFlow()
    
    // 更新时间（前一个交易日）
    private val _updateDate = MutableStateFlow("")
    val updateDate: StateFlow<String> = _updateDate.asStateFlow()
    
    /**
     * 添加持仓（根据购买金额和日期自动计算份额）
     * @param fundCode 基金代码
     * @param fundName 基金名称
     * @param buyDate 购买日期
     * @param buyAmount 购买金额
     */
    fun addPositionByAmount(
        fundCode: String,
        fundName: String,
        buyDate: String,
        buyAmount: Double
    ) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            
            try {
                // 获取当前日期
                val currentDate = com.example.jjsj.util.DateUtils.getCurrentDate()
                
                // 判断是否是当日购买
                val isTodayPurchase = buyDate == currentDate
                
                // 1. 查询购买日期的净值
                val navResult = fundRepository.getFundNavByDate(fundCode, buyDate)
                
                navResult.onSuccess { result ->
                    if (result == null) {
                        _operationState.value = OperationState.Error("无法获取该日期的净值")
                        return@launch
                    }
                    
                    val (buyNav, actualDate) = result
                    
                    if (buyNav == 0.0) {
                        _operationState.value = OperationState.Error("无法获取该日期的净值")
                        return@launch
                    }
                    
                    // 2. 计算份额 = 购买金额 / 购买净值，保留两位小数
                    val shares = round(buyAmount / buyNav * 100) / 100
                    
                    // 3. 判断净值确认状态
                    val navConfirmed = !isTodayPurchase || (actualDate < currentDate)
                    
                    // 4. 创建持仓记录
                    val position = Position(
                        fundCode = fundCode,
                        fundName = fundName,
                        shares = shares,
                        buyNav = buyNav,
                        buyDate = actualDate,  // 使用实际交易日期
                        buyAmount = buyAmount,
                        currentNav = buyNav,
                        navConfirmed = navConfirmed
                    )
                    
                    // 5. 保存到数据库
                    repository.addPosition(position)
                        .onSuccess {
                            val message = when {
                                !navConfirmed -> "添加成功（净值待确认）：份额 $shares"
                                actualDate != buyDate -> "添加成功（使用 $actualDate 净值）：份额 $shares"
                                else -> "添加成功：份额 $shares"
                            }
                            _operationState.value = OperationState.Success(message)
                        }
                        .onFailure { error ->
                            _operationState.value = OperationState.Error(error.message ?: "保存失败")
                        }
                }.onFailure { error ->
                    // 如果是当日购买且获取不到净值，允许添加但标记为待确认
                    if (isTodayPurchase) {
                        // 使用临时净值（比如最新已知净值）创建持仓
                        val position = Position(
                            fundCode = fundCode,
                            fundName = fundName,
                            shares = 0.0,  // 份额待确认
                            buyNav = 0.0,  // 净值待确认
                            buyDate = buyDate,
                            buyAmount = buyAmount,
                            currentNav = 0.0,
                            navConfirmed = false
                        )
                        
                        repository.addPosition(position)
                            .onSuccess {
                                _operationState.value = OperationState.Success("添加成功（净值待确认，请稍后刷新）")
                            }
                            .onFailure { err ->
                                _operationState.value = OperationState.Error(err.message ?: "保存失败")
                            }
                    } else {
                        _operationState.value = OperationState.Error(error.message ?: "查询净值失败")
                    }
                }
            } catch (e: Exception) {
                _operationState.value = OperationState.Error(e.message ?: "添加失败")
            }
        }
    }
    
    /**
     * 添加持仓（直接方式，保留以兼容现有代码）
     */
    fun addPosition(position: Position) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            repository.addPosition(position)
                .onSuccess {
                    _operationState.value = OperationState.Success("添加成功")
                }
                .onFailure { error ->
                    _operationState.value = OperationState.Error(error.message ?: "添加失败")
                }
        }
    }
    
    /**
     * 更新持仓
     */
    fun updatePosition(position: Position) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            repository.updatePosition(position)
                .onSuccess {
                    _operationState.value = OperationState.Success("更新成功")
                }
                .onFailure { error ->
                    _operationState.value = OperationState.Error(error.message ?: "更新失败")
                }
        }
    }
    
    /**
     * 删除某个基金的所有持仓记录
     */
    fun deleteAllPositionsByFundCode(fundCode: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            try {
                val result = repository.deleteAllPositionsByFundCode(fundCode)
                result.onSuccess {
                    _operationState.value = OperationState.Success("删除成功")
                }
                .onFailure { error ->
                    _operationState.value = OperationState.Error(error.message ?: "删除失败")
                }
            } catch (e: Exception) {
                _operationState.value = OperationState.Error(e.message ?: "删除失败")
            }
        }
    }
    
    /**
     * 清空所有持仓
     */
    fun clearAllPositions() {
        viewModelScope.launch {
            repository.clearAllPositions()
        }
    }
    
    /**
     * 重置操作状态
     */
    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }
    
    /**
     * 根据基金代码获取基金名称
     */
    fun fetchFundName(fundCode: String) {
        if (fundCode.isBlank()) {
            _fundNameState.value = null
            return
        }
        
        viewModelScope.launch {
            fundRepository.getFundNameByCode(fundCode)
                .onSuccess { name ->
                    _fundNameState.value = name
                }
                .onFailure {
                    _fundNameState.value = null
                }
        }
    }
    
    /**
     * 重置基金名称状态
     */
    fun resetFundNameState() {
        _fundNameState.value = null
    }
    
    /**
     * 检查是否持有某个基金
     * @return 持有的总份额，0表示未持有
     */
    suspend fun checkHolding(fundCode: String): Double {
        return repository.getTotalSharesByFundCode(fundCode)
    }
    
    /**
     * 卖出基金
     * @param fundCode 基金代码
     * @param sellAmount 卖出金额
     * @param currentNav 当前净值
     */
    fun sellPosition(
        fundCode: String,
        sellAmount: Double,
        currentNav: Double
    ) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            
            repository.sellPosition(fundCode, sellAmount, currentNav)
                .onSuccess { sharesToSell ->
                    _operationState.value = OperationState.Success(
                        "卖出成功：${"%.2f".format(sharesToSell)}份，金额${"%.2f".format(sellAmount)}元"
                    )
                }
                .onFailure { error ->
                    _operationState.value = OperationState.Error(error.message ?: "卖出失败")
                }
        }
    }
    
    
    /**
     * 检查并确认待确认的净值
     * 在App启动时调用
     */
    fun checkAndConfirmPendingNav() {
        viewModelScope.launch {
            try {
                val positions = repository.getAllPositionsSync()
                val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    .format(java.util.Date())
                
                positions.filter { !it.navConfirmed }.forEach { position ->
                    // 检查今天的日期是否在购买日期之后
                    val buyDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        .parse(position.buyDate)
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        .parse(currentDate)
                    
                    if (buyDate != null && today != null && today.after(buyDate)) {
                        // 获取该基金的历史净值数据
                        val navHistory = fundRepository.getFundNavHistory(position.fundCode, pageSize = 10)
                            .getOrNull()
                        
                        // 查找购买日的净值
                        val buyDayNav = navHistory?.find { it.date == position.buyDate }
                        
                        if (buyDayNav != null) {
                            // 找到了购买日净值，更新持仓
                            val updatedPosition = position.copy(
                                buyNav = buyDayNav.nav,
                                shares = position.buyAmount / buyDayNav.nav,
                                navConfirmed = true
                            )
                            repository.updatePosition(updatedPosition)
                            println("✅ 自动确认净值：${position.fundName}，净值：${buyDayNav.nav}")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 获取前一个交易日
     */
    fun getPreviousTradingDate() {
        viewModelScope.launch {
            try {
                // 简单逻辑：获取任意一个基金的净值历史，取第一条的日期作为前一个交易日
                val positions = aggregatedPositions.value
                if (positions.isNotEmpty()) {
                    val navHistory = fundRepository.getFundNavHistory(
                        positions.first().fundCode, 
                        pageSize = 1
                    ).getOrNull()
                    
                    if (navHistory != null && navHistory.isNotEmpty()) {
                        _updateDate.value = navHistory.first().date
                    } else {
                        // 如果没有数据，使用当前日期
                        _updateDate.value = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            .format(java.util.Date())
                    }
                } else {
                    _updateDate.value = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        .format(java.util.Date())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _updateDate.value = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    .format(java.util.Date())
            }
        }
    }
}

/**
 * 操作状态
 */
sealed class OperationState {
    object Idle : OperationState()
    object Loading : OperationState()
    data class Success(val message: String) : OperationState()
    data class Error(val message: String) : OperationState()
}

