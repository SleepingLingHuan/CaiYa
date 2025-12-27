package com.example.jjsj.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jjsj.data.local.entity.FundDetailCacheEntity
import com.example.jjsj.data.model.Fund
import com.example.jjsj.data.model.FundDetail
import com.example.jjsj.data.model.FundNav
import com.example.jjsj.data.remote.FundRankingApi
import com.example.jjsj.data.repository.FundRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 基金ViewModel
 */
class FundViewModel(
    private val repository: FundRepository
) : ViewModel() {
    
    // 基金列表状态（兼容旧代码，使用涨幅榜）
    private val _fundListState = MutableStateFlow<UiState<List<Fund>>>(UiState.Loading)
    val fundListState: StateFlow<UiState<List<Fund>>> = _fundListState.asStateFlow()
    
    // 涨幅榜状态
    private val _topGainersState = MutableStateFlow<UiState<List<Fund>>>(UiState.Loading)
    val topGainersState: StateFlow<UiState<List<Fund>>> = _topGainersState.asStateFlow()
    
    // 跌幅榜状态
    private val _topLosersState = MutableStateFlow<UiState<List<Fund>>>(UiState.Loading)
    val topLosersState: StateFlow<UiState<List<Fund>>> = _topLosersState.asStateFlow()
    
    // 数据时间
    private val _dataDate = MutableStateFlow("")
    val dataDate: StateFlow<String> = _dataDate.asStateFlow()
    
    // 搜索结果状态
    private val _searchResultState = MutableStateFlow<UiState<List<Fund>>>(UiState.Idle)
    val searchResultState: StateFlow<UiState<List<Fund>>> = _searchResultState.asStateFlow()
    
    // 基金详情状态
    private val _fundDetailState = MutableStateFlow<UiState<FundDetail>>(UiState.Idle)
    val fundDetailState: StateFlow<UiState<FundDetail>> = _fundDetailState.asStateFlow()
    
    // 基金详细信息状态（包含基本概况、投资策略等）
    private val _fundDetailInfoState = MutableStateFlow<UiState<FundDetailCacheEntity?>>(UiState.Idle)
    val fundDetailInfoState: StateFlow<UiState<FundDetailCacheEntity?>> = _fundDetailInfoState.asStateFlow()
    
    // 基金净值历史状态（默认7条）
    private val _fundNavHistoryState = MutableStateFlow<UiState<List<FundNav>>>(UiState.Idle)
    val fundNavHistoryState: StateFlow<UiState<List<FundNav>>> = _fundNavHistoryState.asStateFlow()
    
    // 基金净值历史状态（完整60条，用于弹窗）
    private val _fundNavHistoryFullState = MutableStateFlow<UiState<List<FundNav>>>(UiState.Idle)
    val fundNavHistoryFullState: StateFlow<UiState<List<FundNav>>> = _fundNavHistoryFullState.asStateFlow()
    
    // 自选基金列表
    val favoriteFunds: StateFlow<List<Fund>> = repository.getFavoriteFunds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        loadRankings()
    }
    
    /**
     * 加载涨跌幅榜（同时加载涨幅榜和跌幅榜）
     */
    fun loadRankings(
        forceRefresh: Boolean = false,
        sortType: FundRankingApi.SortType = FundRankingApi.SortType.DAY
    ) {
        loadTopGainers(forceRefresh, sortType)
        loadTopLosers(forceRefresh, sortType)
    }
    
    /**
     * 加载涨幅榜
     */
    fun loadTopGainers(
        forceRefresh: Boolean = false,
        sortType: FundRankingApi.SortType = FundRankingApi.SortType.DAY
    ) {
        viewModelScope.launch {
            _topGainersState.value = UiState.Loading
            val result = repository.getTopGainersWithDate(forceRefresh, sortType)
            result.onSuccess { (funds, dataDate) ->
                _topGainersState.value = UiState.Success(funds)
                _fundListState.value = UiState.Success(funds) // 兼容旧代码
                _dataDate.value = dataDate  // 更新数据时间
            }.onFailure { error ->
                _topGainersState.value = UiState.Error(error.message ?: "加载失败")
                _fundListState.value = UiState.Error(error.message ?: "加载失败")
            }
        }
    }
    
    /**
     * 加载跌幅榜
     */
    fun loadTopLosers(
        forceRefresh: Boolean = false,
        sortType: FundRankingApi.SortType = FundRankingApi.SortType.DAY
    ) {
        viewModelScope.launch {
            _topLosersState.value = UiState.Loading
            repository.getTopLosers(forceRefresh, sortType)
                .onSuccess { funds ->
                    _topLosersState.value = UiState.Success(funds)
                }
                .onFailure { error ->
                    _topLosersState.value = UiState.Error(error.message ?: "加载失败")
                }
        }
    }
    
    /**
     * 加载基金列表（兼容旧代码）
     */
    fun loadFundList(
        forceRefresh: Boolean = false,
        sortType: FundRankingApi.SortType = FundRankingApi.SortType.DAY
    ) {
        loadRankings(forceRefresh, sortType)
    }
    
    /**
     * 搜索基金
     */
    fun searchFunds(keyword: String) {
        if (keyword.isBlank()) {
            _searchResultState.value = UiState.Idle
            return
        }
        
        viewModelScope.launch {
            _searchResultState.value = UiState.Loading
            repository.searchFunds(keyword)
                .onSuccess { funds ->
                    _searchResultState.value = UiState.Success(funds)
                }
                .onFailure { error ->
                    _searchResultState.value = UiState.Error(error.message ?: "搜索失败")
                }
        }
    }
    
    /**
     * 加载基金详情
     */
    fun loadFundDetail(code: String) {
        viewModelScope.launch {
            _fundDetailState.value = UiState.Loading
            repository.getFundDetail(code)
                .onSuccess { detail ->
                    _fundDetailState.value = UiState.Success(detail)
                }
                .onFailure { error ->
                    _fundDetailState.value = UiState.Error(error.message ?: "加载详情失败")
                }
        }
    }
    
    /**
     * 加载基金详细信息（从HTML抓取）
     * 包含基金基本概况、投资策略等详细信息
     */
    fun loadFundDetailInfo(code: String) {
        viewModelScope.launch {
            _fundDetailInfoState.value = UiState.Loading
            repository.getFundDetailInfo(code)
                .onSuccess { detailInfo ->
                    _fundDetailInfoState.value = UiState.Success(detailInfo)
                }
                .onFailure { error ->
                    _fundDetailInfoState.value = UiState.Error(error.message ?: "加载基金详情失败")
                }
        }
    }
    
    /**
     * 加载基金净值历史（默认7条）
     */
    fun loadFundNavHistory(fundCode: String) {
        viewModelScope.launch {
            _fundNavHistoryState.value = UiState.Loading
            repository.getFundNavHistory(fundCode, pageSize = 7)
                .onSuccess { navList ->
                    _fundNavHistoryState.value = UiState.Success(navList)
                }
                .onFailure { error ->
                    _fundNavHistoryState.value = UiState.Error(error.message ?: "加载净值历史失败")
                }
        }
    }
    
    /**
     * 加载基金净值历史（完整60条）
     */
    fun loadFundNavHistoryFull(fundCode: String) {
        viewModelScope.launch {
            _fundNavHistoryFullState.value = UiState.Loading
            repository.getFundNavHistory(fundCode, pageSize = 60)
                .onSuccess { navList ->
                    _fundNavHistoryFullState.value = UiState.Success(navList)
                }
                .onFailure { error ->
                    _fundNavHistoryFullState.value = UiState.Error(error.message ?: "加载净值历史失败")
                }
        }
    }
    
    /**
     * 添加自选
     */
    fun addFavorite(fund: Fund) {
        viewModelScope.launch {
            repository.addFavorite(fund)
        }
    }
    
    /**
     * 获取基金详情（用于今日收益计算）
     */
    suspend fun getFundDetailForRevenue(code: String): Result<Fund> {
        return repository.getFundDetail(code).map { it.fund }
    }
    
    /**
     * 删除自选
     */
    fun removeFavorite(fundCode: String) {
        viewModelScope.launch {
            repository.removeFavorite(fundCode)
        }
    }
    
    /**
     * 检查是否已收藏
     */
    suspend fun isFavorite(fundCode: String): Boolean {
        return repository.isFavorite(fundCode)
    }
}

/**
 * UI状态封装
 */
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

