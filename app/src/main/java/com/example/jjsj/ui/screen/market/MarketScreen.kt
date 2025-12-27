package com.example.jjsj.ui.screen.market

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jjsj.data.model.Fund
import com.example.jjsj.data.model.IndexData
import com.example.jjsj.ui.component.*
import com.example.jjsj.ui.navigation.RankingType
import com.example.jjsj.viewmodel.FundViewModel
import com.example.jjsj.viewmodel.IndexViewModel
import com.example.jjsj.viewmodel.UiState
import kotlinx.coroutines.launch

/**
 * 行情页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    viewModel: FundViewModel,
    positionViewModel: com.example.jjsj.viewmodel.PositionViewModel,
    onFundClick: (String) -> Unit,
    onRankingClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val topGainersState by viewModel.topGainersState.collectAsState()
    val topLosersState by viewModel.topLosersState.collectAsState()
    val searchResultState by viewModel.searchResultState.collectAsState()
    val favoriteFunds by viewModel.favoriteFunds.collectAsState()
    val aggregatedPositions by positionViewModel.aggregatedPositions.collectAsState()
    val dataDate by viewModel.dataDate.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    
    // 时间维度选择状态
    var selectedSortType by remember { mutableStateOf(com.example.jjsj.data.remote.FundRankingApi.SortType.DAY) }
    
    // 指数ViewModel
    val indexViewModel: IndexViewModel = viewModel()
    val indicesState by indexViewModel.indicesState.collectAsState()
    
    // Coroutine scope for refresh button
    val coroutineScope = rememberCoroutineScope()
    
    // 存储持仓基金的今日数据
    var positionFundsData by remember { mutableStateOf<Map<String, Fund>>(emptyMap()) }
    
    // 加载持仓基金的今日数据
    LaunchedEffect(aggregatedPositions) {
        if (aggregatedPositions.isNotEmpty()) {
            val fundCodes = aggregatedPositions.filter { !it.hasUnconfirmedNav }.map { it.fundCode }
            val fundsMap = mutableMapOf<String, Fund>()
            fundCodes.forEach { code ->
                viewModel.getFundDetailForRevenue(code).onSuccess { fund ->
                    fundsMap[code] = fund
                }
            }
            positionFundsData = fundsMap
        }
    }
    
    // 计算今日收益数据（排除净值未确认的持仓）
    val positionsWithRevenue = remember(aggregatedPositions, positionFundsData) {
        aggregatedPositions.filter { !it.hasUnconfirmedNav }.mapNotNull { position ->
            val fund = positionFundsData[position.fundCode]
            if (fund != null) {
                com.example.jjsj.ui.component.PositionWithRevenue(
                    position = position,
                    changeRate = fund.changeRate,
                    changeAmount = fund.changeAmount,
                    todayRevenue = fund.changeAmount * position.totalShares,
                    todayRevenueRate = fund.changeRate
                )
            } else null
        }
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // 顶部搜索栏
        TopAppBar(
            title = { Text("基金行情") },
            actions = {
                // 刷新按钮
                IconButton(onClick = { 
                    indexViewModel.loadIndices()
                    viewModel.loadRankings(forceRefresh = true, sortType = selectedSortType)
                    // 刷新持仓基金数据以更新今日收益
                    coroutineScope.launch {
                        val fundCodes = aggregatedPositions.filter { !it.hasUnconfirmedNav }.map { it.fundCode }
                        val fundsMap = mutableMapOf<String, Fund>()
                        fundCodes.forEach { code ->
                            viewModel.getFundDetailForRevenue(code).onSuccess { fund ->
                                fundsMap[code] = fund
                            }
                        }
                        positionFundsData = fundsMap
                    }
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新")
                }
                // 搜索按钮
                IconButton(onClick = { isSearching = !isSearching }) {
                    Icon(Icons.Default.Search, contentDescription = "搜索")
                }
            },
            windowInsets = WindowInsets(top = 8.dp, bottom = 0.dp)
        )
        
        // 搜索输入框
        if (isSearching) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    viewModel.searchFunds(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("输入基金名称或代码") },
                singleLine = true
            )
        }
        
        // 内容区域
        when {
            isSearching && searchQuery.isNotEmpty() -> {
                // 显示搜索结果
                SearchResultContent(
                    state = searchResultState,
                    onFundClick = onFundClick,
                    onRetry = { viewModel.searchFunds(searchQuery) }
                )
            }
            else -> {
                // 显示主页面内容
                MainMarketContent(
                    topGainersState = topGainersState,
                    topLosersState = topLosersState,
                    domesticIndicesState = indexViewModel.domesticIndicesState.collectAsState().value,
                    globalIndicesState = indexViewModel.globalIndicesState.collectAsState().value,
                    positionsWithRevenue = positionsWithRevenue,
                    dataDate = dataDate,
                    selectedSortType = selectedSortType,
                    onSortTypeChange = { newSortType ->
                        selectedSortType = newSortType
                        viewModel.loadRankings(forceRefresh = true, sortType = newSortType)
                    },
                    onFundClick = onFundClick,
                    onRankingClick = onRankingClick,
                    onRetry = { viewModel.loadRankings(forceRefresh = true, sortType = selectedSortType) }
                )
            }
        }
    }
}

@Composable
private fun MainMarketContent(
    topGainersState: UiState<List<Fund>>,
    topLosersState: UiState<List<Fund>>,
    domesticIndicesState: UiState<List<IndexData>>,
    globalIndicesState: UiState<List<IndexData>>,
    positionsWithRevenue: List<com.example.jjsj.ui.component.PositionWithRevenue>,
    dataDate: String,
    selectedSortType: com.example.jjsj.data.remote.FundRankingApi.SortType,
    onSortTypeChange: (com.example.jjsj.data.remote.FundRankingApi.SortType) -> Unit,
    onFundClick: (String) -> Unit,
    onRankingClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 指数横向滚动区域
        item {
            IndicesSection(
                domesticIndicesState = domesticIndicesState,
                globalIndicesState = globalIndicesState
            )
        }
        
        // 今日实时收益区域
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                if (positionsWithRevenue.isNotEmpty()) {
                    TodayRevenueCard(
                        positionsWithRevenue = positionsWithRevenue,
                        onFundClick = onFundClick
                    )
                } else {
                    EmptyTodayRevenueCard()
                }
            }
        }
        
        // 涨幅榜
        when (topGainersState) {
            is UiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is UiState.Success -> {
                if (topGainersState.data.isNotEmpty()) {
                    item {
                        ExpandedRankingCardWithTimePeriod(
                            title = "涨幅榜",
                            funds = topGainersState.data.take(10),
                            allFunds = topGainersState.data,
                            dataDate = dataDate,
                            selectedSortType = selectedSortType,
                            onSortTypeChange = onSortTypeChange,
                            onMoreClick = { onRankingClick(RankingType.TOP_GAINERS) },
                            onFundClick = onFundClick
                        )
                    }
                }
            }
            is UiState.Error -> {
                item {
                    ErrorState(
                        message = topGainersState.message,
                        onRetry = onRetry
                    )
                }
            }
            else -> {}
        }
        
        // 跌幅榜
        when (topLosersState) {
            is UiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is UiState.Success -> {
                if (topLosersState.data.isNotEmpty()) {
                    item {
                        ExpandedRankingCardWithTimePeriod(
                            title = "跌幅榜",
                            funds = topLosersState.data.take(10),
                            allFunds = topLosersState.data,
                            dataDate = dataDate,
                            selectedSortType = selectedSortType,
                            onSortTypeChange = onSortTypeChange,
                            onMoreClick = { onRankingClick(RankingType.TOP_LOSERS) },
                            onFundClick = onFundClick
                        )
                    }
                }
            }
            is UiState.Error -> {
                item {
                    ErrorState(
                        message = topLosersState.message,
                        onRetry = onRetry
                    )
                }
            }
            else -> {}
        }
    }
}

/**
 * 指数横向滚动区域（带选项卡）
 */
@Composable
private fun IndicesSection(
    domesticIndicesState: UiState<List<IndexData>>,
    globalIndicesState: UiState<List<IndexData>>
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Column {
        // 标题栏和选项卡
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "指数行情",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("A股") }
                )
                FilterChip(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("全球") }
                )
            }
        }
        
        // 指数内容
        val currentState = if (selectedTab == 0) domesticIndicesState else globalIndicesState
        
        when (currentState) {
            is UiState.Success -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    currentState.data.forEach { index ->
                        IndexCard(index = index)
                    }
                }
            }
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {}
        }
    }
}

/**
 * 榜单区域
 */
@Composable
private fun RankingSection(
    funds: List<Fund>,
    onRankingClick: (String) -> Unit,
    onFundClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 涨幅榜
            RankingCard(
                title = "涨幅榜",
                funds = funds.sortedByDescending { it.changeRate }.take(5),
                onMoreClick = { onRankingClick(RankingType.TOP_GAINERS) },
                onFundClick = onFundClick,
                modifier = Modifier.weight(1f)
            )
            
            // 跌幅榜
            RankingCard(
                title = "跌幅榜",
                funds = funds.sortedBy { it.changeRate }.take(5),
                onMoreClick = { onRankingClick(RankingType.TOP_LOSERS) },
                onFundClick = onFundClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 榜单卡片
 */
@Composable
private fun RankingCard(
    title: String,
    funds: List<Fund>,
    onMoreClick: () -> Unit,
    onFundClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.clickable(onClick = onMoreClick),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "更多",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "更多",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            funds.forEachIndexed { index, fund ->
                RankingFundItem(
                    rank = index + 1,
                    fund = fund,
                    onClick = { onFundClick(fund.code) }
                )
                if (index < funds.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * 榜单中的基金项
 */
@Composable
private fun RankingFundItem(
    rank: Int,
    fund: Fund,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fund.name,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }
        }
        
        Text(
            text = String.format("%+.2f%%", fund.changeRate),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (fund.changeRate >= 0) 
                androidx.compose.ui.graphics.Color(0xFFE53935) 
            else 
                androidx.compose.ui.graphics.Color(0xFF43A047)
        )
    }
}

/**
 * 放大版榜单卡片（占满宽度）
 */
@Composable
private fun ExpandedRankingCard(
    title: String,
    funds: List<Fund>,
    onMoreClick: () -> Unit,
    onFundClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.clickable(onClick = onMoreClick),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "查看更多",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "更多",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            funds.forEachIndexed { index, fund ->
                ExpandedRankingFundItem(
                    rank = index + 1,
                    fund = fund,
                    onClick = { onFundClick(fund.code) }
                )
                if (index < funds.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

/**
 * 放大版基金项
 */
@Composable
private fun ExpandedRankingFundItem(
    rank: Int,
    fund: Fund,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // 排名
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                // 前三名使用奖牌emoji，其他显示数字
                Text(
                    text = when (rank) {
                        1 -> "🏅"  // 金牌
                        2 -> "🥈"  // 银牌
                        3 -> "🥉"  // 铜牌
                        else -> "$rank"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (rank > 3) MaterialTheme.colorScheme.onSurfaceVariant else androidx.compose.ui.graphics.Color.Unspecified
                )
            }
            
            // 基金信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fund.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = fund.code,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // 涨跌幅
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = String.format("%+.2f%%", fund.changeRate),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (fund.changeRate >= 0) 
                    androidx.compose.ui.graphics.Color(0xFFE53935) 
                else 
                    androidx.compose.ui.graphics.Color(0xFF43A047)
            )
            Text(
                text = com.example.jjsj.util.FormatUtils.formatNav(fund.nav),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FundListContent(
    state: UiState<List<Fund>>,
    onFundClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    when (state) {
        is UiState.Loading -> {
            LoadingIndicator()
        }
        is UiState.Success -> {
            if (state.data.isEmpty()) {
                EmptyState(message = "暂无基金数据")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.data) { fund ->
                        FundListItem(
                            fund = fund,
                            onClick = { onFundClick(fund.code) }
                        )
                    }
                }
            }
        }
        is UiState.Error -> {
            ErrorState(
                message = state.message,
                onRetry = onRetry
            )
        }
        is UiState.Idle -> {
            // 不显示任何内容
        }
    }
}

@Composable
private fun SearchResultContent(
    state: UiState<List<Fund>>,
    onFundClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    when (state) {
        is UiState.Loading -> {
            LoadingIndicator(message = "搜索中...")
        }
        is UiState.Success -> {
            if (state.data.isEmpty()) {
                EmptyState(message = "未找到相关基金")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.data) { fund ->
                        FundListItem(
                            fund = fund,
                            onClick = { onFundClick(fund.code) },
                            showNavAndRate = false  // 搜索模式不显示净值和涨跌幅
                        )
                    }
                }
            }
        }
        is UiState.Error -> {
            ErrorState(
                message = state.message,
                onRetry = onRetry
            )
        }
        is UiState.Idle -> {
            EmptyState(message = "请输入搜索关键词")
        }
    }
}

/**
 * 空状态今日收益卡片
 */
@Composable
private fun EmptyTodayRevenueCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "今日实时收益",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "暂无持仓数据",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "请在持仓管理中添加基金持仓",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 带时间维度选择的榜单卡片
 */
@Composable
private fun ExpandedRankingCardWithTimePeriod(
    title: String,
    funds: List<Fund>,
    allFunds: List<Fund>,  // 全部数据，仅用于显示总数
    dataDate: String,  // 数据时间，如"2025-10-20"
    selectedSortType: com.example.jjsj.data.remote.FundRankingApi.SortType,
    onSortTypeChange: (com.example.jjsj.data.remote.FundRankingApi.SortType) -> Unit,
    onMoreClick: () -> Unit,
    onFundClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (dataDate.isNotEmpty()) {
                        Text(
                            text = "截止$dataDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(
                    modifier = Modifier.clickable(onClick = onMoreClick),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "查看更多",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "更多",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 时间维度选择按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimePeriodChip(
                    label = "今日",
                    selected = selectedSortType == com.example.jjsj.data.remote.FundRankingApi.SortType.DAY,
                    onClick = { onSortTypeChange(com.example.jjsj.data.remote.FundRankingApi.SortType.DAY) }
                )
                TimePeriodChip(
                    label = "近一周",
                    selected = selectedSortType == com.example.jjsj.data.remote.FundRankingApi.SortType.WEEK,
                    onClick = { onSortTypeChange(com.example.jjsj.data.remote.FundRankingApi.SortType.WEEK) }
                )
                TimePeriodChip(
                    label = "近一月",
                    selected = selectedSortType == com.example.jjsj.data.remote.FundRankingApi.SortType.MONTH,
                    onClick = { onSortTypeChange(com.example.jjsj.data.remote.FundRankingApi.SortType.MONTH) }
                )
                TimePeriodChip(
                    label = "近一年",
                    selected = selectedSortType == com.example.jjsj.data.remote.FundRankingApi.SortType.YEAR,
                    onClick = { onSortTypeChange(com.example.jjsj.data.remote.FundRankingApi.SortType.YEAR) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 基金列表
            funds.forEachIndexed { index, fund ->
                ExpandedRankingFundItem(
                    rank = index + 1,
                    fund = fund,
                    onClick = { onFundClick(fund.code) }
                )
                if (index < funds.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

/**
 * 时间维度选择芯片
 */
@Composable
private fun TimePeriodChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

