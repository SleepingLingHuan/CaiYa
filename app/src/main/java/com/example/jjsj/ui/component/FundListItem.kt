package com.example.jjsj.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jjsj.data.model.Fund
import com.example.jjsj.util.FormatUtils

/**
 * 基金列表项组件 - 现代设计
 */
@Composable
fun FundListItem(
    fund: Fund,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showNavAndRate: Boolean = true  // 是否显示净值和涨跌幅
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFAFAFA)  // Very light gray - matches transaction card
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：基金名称和代码
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = fund.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = fund.code,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 右侧：基金类型（搜索模式）或净值和涨跌幅（正常模式）
            if (showNavAndRate) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = FormatUtils.formatNav(fund.nav),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = FormatUtils.formatRate(fund.changeRate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = getRateColor(fund.changeRate),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                // 搜索模式：显示基金类型
                if (fund.type.isNotBlank() && fund.type != "未知") {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = fund.type,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 获取涨跌幅颜色
 */
@Composable
private fun getRateColor(rate: Double): Color {
    return when {
        rate > 0 -> Color(0xFFDC2626) // 现代红色（上涨）
        rate < 0 -> Color(0xFF059669) // 现代绿色（下跌）
        else -> MaterialTheme.colorScheme.onSurface
    }
}

