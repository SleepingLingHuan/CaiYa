package com.example.jjsj.ui.screen.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.jjsj.data.model.FundDetail
import com.example.jjsj.ui.component.ErrorState
import com.example.jjsj.ui.component.LoadingIndicator
import com.example.jjsj.util.DateUtils
import com.example.jjsj.util.FormatUtils
import com.example.jjsj.viewmodel.FundViewModel
import com.example.jjsj.viewmodel.PositionViewModel
import com.example.jjsj.viewmodel.OperationState
import com.example.jjsj.viewmodel.UiState
import kotlinx.coroutines.launch

/**
 * 基金详情页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundDetailScreen(
    fundCode: String,
    viewModel: FundViewModel,
    positionViewModel: PositionViewModel,
    transactionViewModel: com.example.jjsj.viewmodel.TransactionViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val detailState by viewModel.fundDetailState.collectAsState()
    val detailInfoState by viewModel.fundDetailInfoState.collectAsState()
    val navHistoryState by viewModel.fundNavHistoryState.collectAsState()
    val navHistoryFullState by viewModel.fundNavHistoryFullState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isFavorite by remember { mutableStateOf(false) }
    var showNavDialog by remember { mutableStateOf(false) }
    var showBuyDialog by remember { mutableStateOf(false) }
    var showSellDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(fundCode) {
        viewModel.loadFundDetail(fundCode)
        viewModel.loadFundDetailInfo(fundCode)  // 加载基金详细信息
        viewModel.loadFundNavHistory(fundCode)   // 加载净值历史
        isFavorite = viewModel.isFavorite(fundCode)
    }
    
    // 监听操作状态并显示Snackbar
    val operationState by positionViewModel.operationState.collectAsState()
    LaunchedEffect(operationState) {
        when (val state = operationState) {
            is OperationState.Success -> {
                android.util.Log.d("FundDetailScreen", "显示成功Snackbar: ${state.message}")
                val result = snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                android.util.Log.d("FundDetailScreen", "Snackbar显示结果: $result")
                positionViewModel.resetOperationState()
            }
            is OperationState.Error -> {
                android.util.Log.d("FundDetailScreen", "显示错误Snackbar: ${state.message}")
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Long
                )
                positionViewModel.resetOperationState()
            }
            else -> {
                android.util.Log.d("FundDetailScreen", "OperationState: $state")
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("基金详情") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                if (detailState is UiState.Success) {
                                    val fund = (detailState as UiState.Success<FundDetail>).data.fund
                                    if (isFavorite) {
                                        viewModel.removeFavorite(fundCode)
                                    } else {
                                        viewModel.addFavorite(fund)
                                    }
                                    isFavorite = !isFavorite
                                }
                            }
                        }
                    ) {
                        Icon(
                            if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "收藏",
                            tint = if (isFavorite) Color(0xFFFDD835) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                windowInsets = WindowInsets(top = 8.dp, bottom = 0.dp)
            )
        },
        bottomBar = {
            // 固定在底部的买入卖出按钮
            if (detailState is UiState.Success) {
                Surface(
                    tonalElevation = 3.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showSellDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("卖出")
                        }
                        Button(
                            onClick = { showBuyDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("买入")
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // 主内容
            when (val state = detailState) {
                is UiState.Loading -> {
                    LoadingIndicator()
                }
                is UiState.Success -> {
                    FundDetailContent(
                        detail = state.data,
                        detailInfo = (detailInfoState as? UiState.Success)?.data,
                        navHistory = (navHistoryState as? UiState.Success)?.data,
                        onShowMoreNav = {
                            viewModel.loadFundNavHistoryFull(fundCode)
                            showNavDialog = true
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
                is UiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadFundDetail(fundCode) }
                    )
                }
                is UiState.Idle -> {
                    // 初始状态
                }
            }
            
            // Snackbar 显示在顶部
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(paddingValues)
                    .padding(top = 8.dp)
            )
        }
        
        // 净值历史弹窗
        if (showNavDialog) {
            NavHistoryDialog(
                navHistoryState = navHistoryFullState,
                onDismiss = { showNavDialog = false }
            )
        }
        
        // 买入对话框
        if (showBuyDialog && detailState is UiState.Success) {
            val detail = (detailState as UiState.Success<FundDetail>).data
            BuyFundDialog(
                fundCode = fundCode,
                fundName = detail.fund.name,
                currentNav = detail.fund.nav,
                positionViewModel = positionViewModel,
                transactionViewModel = transactionViewModel,
                snackbarHostState = snackbarHostState,
                onDismiss = { showBuyDialog = false }
            )
        }
        
        // 卖出对话框
        if (showSellDialog && detailState is UiState.Success) {
            val detail = (detailState as UiState.Success<FundDetail>).data
            SellFundDialog(
                fundCode = fundCode,
                fundName = detail.fund.name,
                currentNav = detail.fund.nav,
                positionViewModel = positionViewModel,
                transactionViewModel = transactionViewModel,
                snackbarHostState = snackbarHostState,
                onDismiss = { showSellDialog = false }
            )
        }
    }
}

@Composable
private fun FundDetailContent(
    detail: FundDetail,
    detailInfo: com.example.jjsj.data.local.entity.FundDetailCacheEntity?,
    navHistory: List<com.example.jjsj.data.model.FundNav>?,
    onShowMoreNav: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 基本信息卡片（简化版：只显示名称、代码、净值、涨跌幅）
        BasicInfoCard(detail)
        
        // 业绩走势图（15个交易日）
        navHistory?.let { navList ->
            if (navList.isNotEmpty()) {
                PerformanceChartCard(navList)
            }
        }
        
        // 净值历史列表（最近7天）
        navHistory?.let { navList ->
            if (navList.isNotEmpty()) {
                NavHistoryCard(navList, onShowMoreNav)
            }
        }
        
        // 基金详细概况（从HTML抓取）
        detailInfo?.let { info ->
            FundDetailInfoSection(info)
        }
        
        // 重仓持股（从detailInfo获取）
        detailInfo?.let { info ->
            val holdings = info.getTopHoldings()
            if (holdings.isNotEmpty()) {
                HoldingsCard(holdings)
            }
        }
    }
}

@Composable
private fun BasicInfoCard(detail: FundDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = detail.fund.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = detail.fund.code,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("最新净值", style = MaterialTheme.typography.bodySmall)
                    Text(
                        FormatUtils.formatNav(detail.fund.nav),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("涨跌幅", style = MaterialTheme.typography.bodySmall)
                    Text(
                        FormatUtils.formatRate(detail.fund.changeRate),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (detail.fund.changeRate >= 0) Color(0xFFE53935) else Color(0xFF43A047)
                    )
                }
            }
        }
    }
}

@Composable
private fun HoldingsCard(holdings: List<com.example.jjsj.data.local.entity.StockHolding>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "十大重仓股",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            holdings.forEach { holding ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = holding.stockName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = holding.stockCode,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = holding.holdingRatio,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, content: String, maxLinesCollapsed: Int = 6) {
    var isExpanded by remember { mutableStateOf(false) }
    var shouldShowButton by remember { mutableStateOf(false) }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (isExpanded) Int.MAX_VALUE else maxLinesCollapsed,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { textLayoutResult ->
                    if (textLayoutResult.hasVisualOverflow) {
                        shouldShowButton = true
                    }
                }
            )
            
            if (shouldShowButton) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "收起" else "查看全部内容",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = if (isExpanded) 
                            Icons.Filled.KeyboardArrowUp 
                        else 
                            Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "收起" else "展开",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * 净值历史卡片（显示最近7天）
 */
@Composable
private fun NavHistoryCard(
    navHistory: List<com.example.jjsj.data.model.FundNav>,
    onShowMore: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "净值历史",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 表头
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "日期",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "单位净值",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "日增长率",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 净值数据列表
            navHistory.forEach { nav ->
                NavHistoryRow(nav)
            }
            
            // "查看更多"按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onShowMore() }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "查看更多净值数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "查看更多",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 净值历史行
 */
@Composable
private fun NavHistoryRow(nav: com.example.jjsj.data.model.FundNav) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = nav.date.substring(5), // 显示 MM-DD
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = String.format("%.4f", nav.nav),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = FormatUtils.formatRate(nav.changeRate),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (nav.changeRate >= 0) Color(0xFFE53935) else Color(0xFF43A047),
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 净值历史弹窗（显示60天数据）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavHistoryDialog(
    navHistoryState: UiState<List<com.example.jjsj.data.model.FundNav>>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.8f)
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "净值历史",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                when (navHistoryState) {
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is UiState.Success -> {
                        val navList = navHistoryState.data
                        
                        // 表头
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "日期",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "单位净值",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "日增长率",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        // 可滚动的净值列表
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            navList.forEach { nav ->
                                NavHistoryRow(nav)
                            }
                        }
                    }
                    is UiState.Error -> {
                        Text(
                            text = "加载失败: ${navHistoryState.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    else -> {}
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 关闭按钮
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("关闭")
                }
            }
        }
    }
}

/**
 * 基金详细信息区域（从HTML抓取的数据）
 */
@Composable
private fun FundDetailInfoSection(
    info: com.example.jjsj.data.local.entity.FundDetailCacheEntity
) {
    // 基本概况
    if (info.fundFullName.isNotEmpty() || info.fundType.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "基本概况",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider()
                
                if (info.fundFullName.isNotEmpty()) {
                    DetailRow("基金全称", info.fundFullName)
                }
                if (info.fundType.isNotEmpty()) {
                    DetailRow("基金类型", info.fundType)
                }
                if (info.establishDate.isNotEmpty()) {
                    DetailRow("成立日期", info.establishDate)
                }
                if (info.assetScale.isNotEmpty()) {
                    DetailRow("资产规模", info.assetScale)
                }
                if (info.fundManager.isNotEmpty()) {
                    DetailRow("基金经理", info.fundManager)
                }
                if (info.fundCompany.isNotEmpty()) {
                    DetailRow("基金公司", info.fundCompany)
                }
                if (info.fundCustodian.isNotEmpty()) {
                    DetailRow("基金托管人", info.fundCustodian)
                }
            }
        }
    }
    
    // 费率信息
    if (info.managementFeeRate.isNotEmpty() || info.custodianFeeRate.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "费率信息",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider()
                
                if (info.managementFeeRate.isNotEmpty()) {
                    DetailRow("管理费率", info.managementFeeRate)
                }
                if (info.custodianFeeRate.isNotEmpty()) {
                    DetailRow("托管费率", info.custodianFeeRate)
                }
                if (info.maxPurchaseFee.isNotEmpty()) {
                    DetailRow("最高申购费率", info.maxPurchaseFee)
                }
                if (info.maxRedemptionFee.isNotEmpty()) {
                    DetailRow("最高赎回费率", info.maxRedemptionFee)
                }
            }
        }
    }
    
    // 投资信息（带折叠功能）
    if (info.investmentObjective.isNotEmpty()) {
        InfoCard(title = "投资目标", content = info.investmentObjective, maxLinesCollapsed = 5)
    }
    if (info.investmentPhilosophy.isNotEmpty()) {
        InfoCard(title = "投资理念", content = info.investmentPhilosophy, maxLinesCollapsed = 5)
    }
    if (info.investmentScope.isNotEmpty()) {
        InfoCard(title = "投资范围", content = info.investmentScope, maxLinesCollapsed = 5)
    }
    if (info.investmentStrategy.isNotEmpty()) {
        InfoCard(title = "投资策略", content = info.investmentStrategy, maxLinesCollapsed = 5)
    }
    if (info.dividendPolicy.isNotEmpty()) {
        InfoCard(title = "分红政策", content = info.dividendPolicy, maxLinesCollapsed = 5)
    }
    if (info.riskReturnCharacter.isNotEmpty()) {
        InfoCard(title = "风险收益特征", content = info.riskReturnCharacter, maxLinesCollapsed = 5)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2f)
        )
    }
}

/**
 * 买入基金对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BuyFundDialog(
    fundCode: String,
    fundName: String,
    currentNav: Double,
    positionViewModel: PositionViewModel,
    transactionViewModel: com.example.jjsj.viewmodel.TransactionViewModel,
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit
) {
    var buyAmount by remember { mutableStateOf("") }
    var buyDate by remember { mutableStateOf(DateUtils.getCurrentDate()) }
    val operationState by positionViewModel.operationState.collectAsState()
    val scope = rememberCoroutineScope()
    
    // 监听操作结果
    LaunchedEffect(operationState) {
        when (val state = operationState) {
            is OperationState.Success -> {
                // 记录交易 - 从成功消息中提取shares，或根据金额和净值计算
                val amount = buyAmount.toDoubleOrNull() ?: 0.0
                // 尝试从消息中提取份额，如果失败则用amount/nav估算
                val shares = try {
                    state.message.substringAfter("购买份额：").substringBefore("份").trim().toDouble()
                } catch (e: Exception) {
                    amount / currentNav
                }
                // 只记录金额和份额大于0的交易
                if (amount > 0 && shares > 0) {
                    transactionViewModel.addTransaction(
                        com.example.jjsj.data.model.Transaction(
                            fundCode = fundCode,
                            fundName = fundName,
                            transactionType = com.example.jjsj.data.model.TransactionType.BUY,
                            transactionDate = buyDate,
                            amount = amount,
                            shares = shares,
                            nav = currentNav
                        )
                    )
                }
                // 关闭对话框，snackbar由外部统一处理
                onDismiss()
            }
            else -> {}
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("买入基金") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 显示基金信息
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = fundName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("代码：$fundCode", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "当前净值：${FormatUtils.formatNav(currentNav)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = buyDate,
                    onValueChange = { buyDate = it },
                    label = { Text("购买日期") },
                    placeholder = { Text("格式：yyyy-MM-dd") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = buyAmount,
                    onValueChange = { buyAmount = it },
                    label = { Text("购买金额（元）") },
                    placeholder = { Text("如：10000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "份额将根据购买日期的净值自动计算",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // 显示操作状态
                when (val state = operationState) {
                    is OperationState.Loading -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    is OperationState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = buyAmount.toDoubleOrNull() ?: 0.0
                    if (amount <= 0) {
                        // 直接关闭对话框并在主界面显示Snackbar
                        onDismiss()
                        scope.launch {
                            snackbarHostState.showSnackbar("金额必须大于0")
                        }
                    } else {
                        positionViewModel.addPositionByAmount(
                            fundCode = fundCode,
                            fundName = fundName,
                            buyDate = buyDate,
                            buyAmount = amount
                        )
                    }
                },
                enabled = buyDate.isNotBlank() && buyAmount.isNotBlank() &&
                        operationState !is OperationState.Loading
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 卖出基金对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellFundDialog(
    fundCode: String,
    fundName: String,
    currentNav: Double,
    positionViewModel: PositionViewModel,
    transactionViewModel: com.example.jjsj.viewmodel.TransactionViewModel,
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit
) {
    var sellShares by remember { mutableStateOf("") }
    var holdingShares by remember { mutableStateOf(0.0) }
    val operationState by positionViewModel.operationState.collectAsState()
    val scope = rememberCoroutineScope()
    
    // 检查持仓
    LaunchedEffect(fundCode) {
        holdingShares = positionViewModel.checkHolding(fundCode)
    }
    
    // 监听操作结果
    LaunchedEffect(operationState) {
        when (val state = operationState) {
            is OperationState.Success -> {
                // 记录交易
                val shares = sellShares.toDoubleOrNull() ?: 0.0
                val amount = shares * currentNav
                // 只记录金额和份额大于0的交易
                if (amount > 0 && shares > 0) {
                    transactionViewModel.addTransaction(
                        com.example.jjsj.data.model.Transaction(
                            fundCode = fundCode,
                            fundName = fundName,
                            transactionType = com.example.jjsj.data.model.TransactionType.SELL,
                            transactionDate = com.example.jjsj.util.DateUtils.getCurrentDate(),
                            amount = amount,
                            shares = shares,
                            nav = currentNav
                        )
                    )
                }
                // 关闭对话框，snackbar由外部统一处理
                onDismiss()
            }
            else -> {}
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("卖出基金") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 显示基金信息
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = fundName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("代码：$fundCode", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "当前净值：${FormatUtils.formatNav(currentNav)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                // 显示持仓情况
                if (holdingShares > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("持有份额：", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${"%.2f".format(holdingShares)}份",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // 卖出份额输入框
                    OutlinedTextField(
                        value = sellShares,
                        onValueChange = { sellShares = it },
                        label = { Text("卖出份额") },
                        placeholder = { Text("如：500.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // 快捷选项按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { sellShares = "%.2f".format(holdingShares) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("全部")
                        }
                        OutlinedButton(
                            onClick = { sellShares = "%.2f".format(holdingShares / 2) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("1/2")
                        }
                        OutlinedButton(
                            onClick = { sellShares = "%.2f".format(holdingShares / 4) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("1/4")
                        }
                    }
                    
                    // 计算卖出金额显示
                    val sellSharesDouble = sellShares.toDoubleOrNull() ?: 0.0
                    if (sellSharesDouble > 0) {
                        val sellAmount = sellSharesDouble * currentNav
                        Text(
                            text = "预计卖出金额：¥${"%.2f".format(sellAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = "卖出按先进先出原则处理",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "您未持有该基金，无法卖出",
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                
                // 显示操作状态
                when (val state = operationState) {
                    is OperationState.Loading -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    is OperationState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            if (holdingShares > 0) {
                val sellSharesDouble = sellShares.toDoubleOrNull() ?: 0.0
                TextButton(
                    onClick = {
                        if (sellSharesDouble <= 0) {
                            // 直接关闭对话框并在主界面显示Snackbar
                            onDismiss()
                            scope.launch {
                                snackbarHostState.showSnackbar("份额必须大于0")
                            }
                        } else if (sellSharesDouble > holdingShares) {
                            onDismiss()
                            scope.launch {
                                snackbarHostState.showSnackbar("卖出份额超过持有份额")
                            }
                        } else {
                            val sellAmount = sellSharesDouble * currentNav
                            positionViewModel.sellPosition(
                                fundCode = fundCode,
                                sellAmount = sellAmount,
                                currentNav = currentNav
                            )
                        }
                    },
                    enabled = sellShares.isNotBlank() && 
                             operationState !is OperationState.Loading
                ) {
                    Text("确认")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (holdingShares > 0) "取消" else "关闭")
            }
        }
    )
}

/**
 * 业绩走势图卡片
 */
@Composable
private fun PerformanceChartCard(
    navHistory: List<com.example.jjsj.data.model.FundNav>
) {
    // 取最近15个交易日数据
    val chartData = navHistory.take(15).reversed() // 反转使时间从左到右
    
    if (chartData.isEmpty()) return
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "业绩走势",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // 显示收益率
            val firstNav = chartData.first().nav
            val lastNav = chartData.last().nav
            val returnRate = ((lastNav - firstNav) / firstNav) * 100
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "近${chartData.size}日",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "本基金",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%+.2f%%", returnRate),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (returnRate >= 0) 
                            androidx.compose.ui.graphics.Color(0xFFE53935) 
                        else 
                            androidx.compose.ui.graphics.Color(0xFF43A047)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 折线图
            LineChart(
                data = chartData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

/**
 * 折线图组件
 */
@Composable
private fun LineChart(
    data: List<com.example.jjsj.data.model.FundNav>,
    modifier: Modifier = Modifier
) {
    val navValues = data.map { it.nav }
    val baseNav = data.first().nav // 基准净值（最早的净值）
    
    // 计算相对于基准的百分比变化
    val percentChanges = data.map { ((it.nav - baseNav) / baseNav) * 100 }
    val minPercent = percentChanges.minOrNull() ?: -5.0
    val maxPercent = percentChanges.maxOrNull() ?: 5.0
    val percentRange = maxPercent - minPercent
    
    // 颜色
    val lineColor = androidx.compose.ui.graphics.Color(0xFFFFB300) // 金色
    val fillColor = lineColor.copy(alpha = 0.2f)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val padding = 50f
            val chartWidth = width - padding * 2
            val chartHeight = height - padding * 2
            
            // 绘制网格线
            for (i in 0..4) {
                val y = padding + (chartHeight * i / 4)
                drawLine(
                    color = gridColor,
                    start = androidx.compose.ui.geometry.Offset(padding, y),
                    end = androidx.compose.ui.geometry.Offset(width - padding, y),
                    strokeWidth = 1f
                )
            }
            
            // 计算点位（基于百分比变化）
            val points = mutableListOf<androidx.compose.ui.geometry.Offset>()
            percentChanges.forEachIndexed { index, percent ->
                val x = padding + (chartWidth * index / (data.size - 1).coerceAtLeast(1))
                val normalizedValue = if (percentRange > 0) (percent - minPercent) / percentRange else 0.5
                val y = padding + chartHeight * (1 - normalizedValue)
                points.add(androidx.compose.ui.geometry.Offset(x, y.toFloat()))
            }
            
            // 绘制填充区域
            if (points.size > 1) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(points.first().x, height - padding)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, height - padding)
                    close()
                }
                drawPath(
                    path = path,
                    color = fillColor
                )
            }
            
            // 绘制折线
            if (points.size > 1) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(points.first().x, points.first().y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(
                    path = path,
                    color = lineColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                )
            }
            
            // 绘制圆点
            points.forEach { point ->
                drawCircle(
                    color = lineColor,
                    radius = 4f,
                    center = point
                )
            }
        }
        
        // 横轴：时间标签
        if (data.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 50.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = data.first().date.substring(5), // 显示 MM-DD
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = data.last().date.substring(5), // 显示 MM-DD
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // 纵轴：百分比标签
        if (percentChanges.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format("%+.1f%%", maxPercent),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = String.format("%+.1f%%", minPercent),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

