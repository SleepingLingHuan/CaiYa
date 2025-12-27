package com.example.jjsj.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jjsj.data.model.IndexData

/**
 * 指数卡片组件 - 现代设计
 */
@Composable
fun IndexCard(
    index: IndexData,
    modifier: Modifier = Modifier
) {
    val isRising = index.changeRate >= 0
    val bgColor = if (isRising) {
        Color(0xFFFEF2F2) // 极淡红色背景
    } else {
        Color(0xFFF0FDF4) // 极淡绿色背景
    }
    
    val textColor = if (isRising) {
        Color(0xFFDC2626) // 现代红色
    } else {
        Color(0xFF059669) // 现代绿色
    }
    
    Card(
        modifier = modifier
            .size(120.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = bgColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isRising) Color(0xFFFECACA) else Color(0xFFBBF7D0)  // Green border for decline
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 指数名称
            Text(
                text = index.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 当前点位
            Text(
                text = String.format("%.2f", index.currentPrice),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // 涨跌幅
            Text(
                text = String.format("%+.2f%%", index.changeRate),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

