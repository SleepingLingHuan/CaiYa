package com.example.jjsj.ui.screen.favorite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.jjsj.data.model.Fund
import com.example.jjsj.ui.component.EmptyState
import com.example.jjsj.ui.component.FundListItem
import com.example.jjsj.viewmodel.FundViewModel

/**
 * 自选基金页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    viewModel: FundViewModel,
    onFundClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteFunds by viewModel.favoriteFunds.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
    var sortedFunds by remember { mutableStateOf<List<Fund>>(emptyList()) }
    
    // 排序方式
    var sortBy by remember { mutableStateOf(SortType.DEFAULT) }
    
    LaunchedEffect(favoriteFunds, sortBy) {
        sortedFunds = when (sortBy) {
            SortType.DEFAULT -> favoriteFunds
            SortType.RATE_DESC -> favoriteFunds.sortedByDescending { it.changeRate }
            SortType.RATE_ASC -> favoriteFunds.sortedBy { it.changeRate }
            SortType.NAV_DESC -> favoriteFunds.sortedByDescending { it.nav }
            SortType.NAV_ASC -> favoriteFunds.sortedBy { it.nav }
        }
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // 顶部栏
        TopAppBar(
            title = { Text("自选基金") },
            actions = {
                // 排序按钮
                TextButton(onClick = { showSortMenu = true }) {
                    Text("排序")
                }
                
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("默认排序") },
                        onClick = {
                            sortBy = SortType.DEFAULT
                            showSortMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("涨幅从高到低") },
                        onClick = {
                            sortBy = SortType.RATE_DESC
                            showSortMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("涨幅从低到高") },
                        onClick = {
                            sortBy = SortType.RATE_ASC
                            showSortMenu = false
                        }
                    )
                }
            },
            windowInsets = WindowInsets(top = 8.dp, bottom = 0.dp)
        )
        
        // 自选列表
        if (sortedFunds.isEmpty()) {
            EmptyState(message = "暂无自选基金\n在基金详情页面点击右上角加入自选")
        } else {
            // 计算汇总数据
            val totalCount = sortedFunds.size
            val upCount = sortedFunds.count { it.changeRate > 0 }
            val downCount = sortedFunds.count { it.changeRate < 0 }
            val averageRate = if (sortedFunds.isNotEmpty()) {
                sortedFunds.map { it.changeRate }.average()
            } else {
                0.0
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 汇总卡片
                item {
                    FavoriteSummaryCard(
                        totalCount = totalCount,
                        upCount = upCount,
                        downCount = downCount,
                        averageRate = averageRate
                    )
                }
                
                // 基金列表
                items(sortedFunds, key = { it.code }) { fund ->
                    SwipeToDeleteItem(
                        fund = fund,
                        onFundClick = { onFundClick(fund.code) },
                        onDelete = { viewModel.removeFavorite(fund.code) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteItem(
    fund: Fund,
    onFundClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )
    
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        FundListItem(
            fund = fund,
            onClick = onFundClick
        )
    }
}

private enum class SortType {
    DEFAULT, RATE_DESC, RATE_ASC, NAV_DESC, NAV_ASC
}

/**
 * 自选基金汇总卡片
 */
@Composable
private fun FavoriteSummaryCard(
    totalCount: Int,
    upCount: Int,
    downCount: Int,
    averageRate: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "自选汇总",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 总数
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = totalCount.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "基金总数",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                // 上涨数
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = upCount.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(0xFFE53935)
                    )
                    Text(
                        text = "上涨",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                // 下跌数
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = downCount.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(0xFF43A047)
                    )
                    Text(
                        text = "下跌",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 平均涨跌幅
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "平均涨跌幅",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = String.format("%+.2f%%", averageRate),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = if (averageRate >= 0) 
                        androidx.compose.ui.graphics.Color(0xFFE53935) 
                    else 
                        androidx.compose.ui.graphics.Color(0xFF43A047)
                )
            }
        }
    }
}

