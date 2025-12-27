package com.example.jjsj.ui.screen.ranking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.jjsj.data.model.Fund
import com.example.jjsj.ui.component.FundListItem
import com.example.jjsj.ui.component.LoadingIndicator
import com.example.jjsj.ui.navigation.RankingType
import com.example.jjsj.viewmodel.FundViewModel
import com.example.jjsj.viewmodel.UiState

/**
 * 榜单详情页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingListScreen(
    type: String,
    viewModel: FundViewModel,
    onBackClick: () -> Unit,
    onFundClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val fundListState by viewModel.fundListState.collectAsState()
    
    val title = when (type) {
        RankingType.TOP_GAINERS -> "涨幅榜"
        RankingType.TOP_LOSERS -> "跌幅榜"
        else -> "榜单"
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when (val state = fundListState) {
            is UiState.Loading -> {
                LoadingIndicator()
            }
            is UiState.Success -> {
                val sortedFunds = when (type) {
                    RankingType.TOP_GAINERS -> state.data.sortedByDescending { it.changeRate }.take(30)
                    RankingType.TOP_LOSERS -> state.data.sortedBy { it.changeRate }.take(30)
                    else -> state.data.take(30)
                }
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(sortedFunds) { index, fund ->
                        RankingFundItem(
                            rank = index + 1,
                            fund = fund,
                            onClick = { onFundClick(fund.code) }
                        )
                    }
                }
            }
            else -> {
                // Empty or error state
            }
        }
    }
}

@Composable
private fun RankingFundItem(
    rank: Int,
    fund: Fund,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 排名
        Text(
            text = "$rank",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .width(40.dp)
                .padding(top = 16.dp)
        )
        
        // 基金卡片
        FundListItem(
            fund = fund,
            onClick = onClick,
            modifier = Modifier.weight(1f)
        )
    }
}

