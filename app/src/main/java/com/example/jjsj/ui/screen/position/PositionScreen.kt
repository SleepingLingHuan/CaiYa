package com.example.jjsj.ui.screen.position

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.jjsj.data.model.Position
import com.example.jjsj.ui.component.EmptyState
import com.example.jjsj.util.DateUtils
import com.example.jjsj.util.FormatUtils
import com.example.jjsj.viewmodel.PositionViewModel
import com.example.jjsj.viewmodel.OperationState

/**
 * 持仓页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PositionScreen(
    viewModel: PositionViewModel,
    onFundClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val aggregatedPositions by viewModel.aggregatedPositions.collectAsState()
    val positionSummary by viewModel.positionSummary.collectAsState()
    val updateDate by viewModel.updateDate.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 启动时检查并确认待确认的净值
    LaunchedEffect(Unit) {
        viewModel.checkAndConfirmPendingNav()
    }
    
    // 加载更新时间
    LaunchedEffect(aggregatedPositions) {
        if (aggregatedPositions.isNotEmpty()) {
            viewModel.getPreviousTradingDate()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("持仓管理") },
                windowInsets = WindowInsets(top = 8.dp, bottom = 0.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        // 持仓列表（聚合显示）
        if (aggregatedPositions.isEmpty()) {
            EmptyState(message = "暂无持仓基金\n在基金详情页面点击下方买入基金")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 汇总卡片
                item {
                    PositionSummaryCard(
                        totalCost = positionSummary.totalCost,
                        totalValue = positionSummary.totalValue,
                        totalProfit = positionSummary.totalProfit,
                        totalProfitRate = positionSummary.totalProfitRate,
                        updateDate = updateDate
                    )
                }
                
                // 持仓列表
                items(aggregatedPositions, key = { it.fundCode }) { position ->
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        AggregatedPositionCard(
                            position = position,
                            onClick = { onFundClick(position.fundCode) }
                        )
                    }
                }
            }
        }
    }
    
    // 显示操作结果
    val operationState by viewModel.operationState.collectAsState()
    LaunchedEffect(operationState) {
        when (val state = operationState) {
            is OperationState.Success -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                viewModel.resetOperationState()
            }
            is OperationState.Error -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Long
                )
                viewModel.resetOperationState()
            }
            else -> {}
        }
    }
}

/**
 * 持仓汇总卡片 - 现代设计
 */
@Composable
private fun PositionSummaryCard(
    totalCost: Double,
    totalValue: Double,
    totalProfit: Double,
    totalProfitRate: Double,
    updateDate: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (totalProfit >= 0) 
                Color(0xFFFEF2F2)  // Light red tint for profit
            else 
                Color(0xFFEFF6FF)  // Light blue tint for loss (no green!)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "持仓汇总",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (updateDate.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = updateDate.substring(5),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Profit/Loss Display - Prominent
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "总盈亏",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = FormatUtils.formatAmount(totalProfit),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (totalProfit >= 0) Color(0xFFDC2626) else Color(0xFF059669)
                )
                Text(
                    text = FormatUtils.formatRate(totalProfitRate),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (totalProfitRate >= 0) Color(0xFFDC2626) else Color(0xFF059669)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))
            
            // Cost and Value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "总成本",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        FormatUtils.formatAmount(totalCost),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "总市值",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        FormatUtils.formatAmount(totalValue),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * 聚合持仓卡片 - 现代设计
 */
@Composable
private fun AggregatedPositionCard(
    position: com.example.jjsj.data.model.AggregatedPosition,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color(0xFFFAFAFA)  // Very light gray - matches transaction card
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with fund name and badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = position.fundName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = position.fundCode,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (position.purchaseCount > 1) {
                            Surface(
                                color = Color(0xFFDEEDFF),
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                Text(
                                    text = "${position.purchaseCount}笔",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = Color(0xFF1E40AF)
                                )
                            }
                        }
                        if (position.hasUnconfirmedNav) {
                            Surface(
                                color = Color(0xFFFEE2E2),
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                Text(
                                    text = "待确认",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
                
                // Profit/Loss indicator on the right
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = FormatUtils.formatAmount(position.totalProfit),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (position.totalProfit >= 0) Color(0xFFDC2626) else Color(0xFF059669)
                    )
                    Text(
                        text = FormatUtils.formatRate(position.profitRate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (position.profitRate >= 0) Color(0xFFDC2626) else Color(0xFF059669)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Price info in a subtle container
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CompactInfoItem("均价", FormatUtils.formatNav(position.averageBuyNav))
                    CompactInfoItem("现价", FormatUtils.formatNav(position.currentNav))
                    CompactInfoItem("份额", "${"%.2f".format(position.totalShares)}")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Cost and Value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem("持仓成本", FormatUtils.formatAmount(position.totalCost))
                InfoItem("当前市值", FormatUtils.formatAmount(position.currentValue))
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CompactInfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

