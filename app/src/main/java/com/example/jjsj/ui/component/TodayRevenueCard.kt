package com.example.jjsj.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jjsj.data.model.AggregatedPosition

/**
 * 今日实时收益卡片数据
 */
data class PositionWithRevenue(
    val position: AggregatedPosition,
    val changeRate: Double,        // 日涨跌幅
    val changeAmount: Double,       // 单位净值日涨跌额
    val todayRevenue: Double,       // 今日收益 = 份额 × 日涨跌额
    val todayRevenueRate: Double    // 今日收益率（等于日涨跌幅）
)

/**
 * 今日实时收益卡片
 * 显示持仓基金的今日收益汇总
 */
@Composable
fun TodayRevenueCard(
    positionsWithRevenue: List<PositionWithRevenue>,
    modifier: Modifier = Modifier,
    onFundClick: (String) -> Unit = {}
) {
    // 计算今日总收益数据
    val totalRevenue = positionsWithRevenue.sumOf { it.todayRevenue }
    val totalCost = positionsWithRevenue.sumOf { it.position.totalCost }
    val totalRevenueRate = if (totalCost > 0) {
        (totalRevenue / totalCost) * 100
    } else 0.0
    
    val gainersCount = positionsWithRevenue.count { it.changeRate > 0 }
    val losersCount = positionsWithRevenue.count { it.changeRate < 0 }
    
    // 按今日收益额降序排列
    val sortedPositions = positionsWithRevenue.sortedByDescending { it.todayRevenue }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题
            Text(
                text = "今日实时收益",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // 总收益汇总区
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (totalRevenueRate >= 0) 
                    Color(0xFFFEF2F2) else Color(0xFFEFF6FF),  // Blue instead of green
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "今日总收益",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%.2f", totalRevenue),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (totalRevenue >= 0) Color(0xFFDC2626) else Color(0xFF059669)
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "今日收益率",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%+.2f%%", totalRevenueRate),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (totalRevenueRate >= 0) Color(0xFFDC2626) else Color(0xFF059669)
                        )
                    }
                }
            }
            
            // 上涨/下跌统计
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$gainersCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                    Text(
                        text = "上涨",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                HorizontalDivider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp)
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$losersCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669)
                    )
                    Text(
                        text = "下跌",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            HorizontalDivider()
            
            // 基金列表标题
            Text(
                text = "基金收益明细",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            
            // 基金收益列表（最多显示10条）
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sortedPositions.take(10).forEach { posWithRev ->
                    TodayRevenueFundItem(
                        positionWithRevenue = posWithRev,
                        onClick = { onFundClick(posWithRev.position.fundCode) }
                    )
                }
                
                if (sortedPositions.size > 10) {
                    Text(
                        text = "还有 ${sortedPositions.size - 10} 只基金...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * 今日收益基金项
 */
@Composable
private fun TodayRevenueFundItem(
    positionWithRevenue: PositionWithRevenue,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = positionWithRevenue.position.fundName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = positionWithRevenue.position.fundCode,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = String.format("%+.2f", positionWithRevenue.todayRevenue),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (positionWithRevenue.todayRevenue >= 0) Color(0xFFDC2626) else Color(0xFF059669)
            )
            Text(
                text = String.format("%+.2f%%", positionWithRevenue.changeRate),
                style = MaterialTheme.typography.bodySmall,
                color = if (positionWithRevenue.changeRate >= 0) Color(0xFFDC2626) else Color(0xFF059669)
            )
        }
    }
}
