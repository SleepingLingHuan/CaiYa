package com.example.jjsj.ui.screen.visual

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jjsj.data.model.Holding
import com.example.jjsj.util.Constants
import kotlin.math.roundToInt

/**
 * 数据可视化页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualScreen(
    positionViewModel: com.example.jjsj.viewmodel.PositionViewModel,
    fundViewModel: com.example.jjsj.viewmodel.FundViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("估值温度计", "持仓桑基图")
    
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("数据可视化") },
            windowInsets = WindowInsets(top = 8.dp, bottom = 0.dp)
        )
        
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        when (selectedTab) {
            0 -> ValuationThermometerScreen()
            1 -> SankeyChartScreen(
                positionViewModel = positionViewModel,
                fundViewModel = fundViewModel
            )
        }
    }
}

/**
 * 估值温度计页面
 */
@Composable
private fun ValuationThermometerScreen() {
    // 使用真实估值数据
    val valuationData = remember { com.example.jjsj.data.model.ValuationDataSource.valuations }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "指数估值温度计",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "基于历史市盈率（PE）和市净率（PB）的百分位，显示当前市场估值水平。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        valuationData.forEach { data ->
            ValuationThermometerCard(valuationData = data)
        }
    }
}

/**
 * 估值温度计卡片（完整版）
 */
@Composable
private fun ValuationThermometerCard(
    valuationData: com.example.jjsj.data.model.ValuationData
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 标题和等级
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = valuationData.indexName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = Color(valuationData.valuationLevel.color).copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = valuationData.valuationLevel.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(valuationData.valuationLevel.color),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // PE数据
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "市盈率(PE)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${valuationData.pe}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // PE温度计
            ValuationProgressBar(
                value = valuationData.pePercentile,
                label = "PE百分位"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // PB数据
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "市净率(PB)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${valuationData.pb}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // PB温度计
            ValuationProgressBar(
                value = valuationData.pbPercentile,
                label = "PB百分位"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            
            // 综合估值
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "综合估值百分位",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${String.format("%.1f", valuationData.compositePercentile)}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(valuationData.valuationLevel.color)
                )
            }
        }
    }
}

/**
 * 估值进度条
 */
@Composable
private fun ValuationProgressBar(
    value: Double,
    label: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${String.format("%.1f", value)}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = getValuationColorByPercentile(value)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // 温度计条形图
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)  // 减小高度，更细
        ) {
            val width = size.width
            val height = size.height
            
            // 绘制背景渐变（淡化颜色）
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF66BB6A), // 淡绿色（极度低估）
                        Color(0xFF81C784), // 浅绿色（低估）
                        Color(0xFFFFD54F), // 淡黄色（正常）
                        Color(0xFFFF8A65), // 淡橙色（高估）
                        Color(0xFFE57373)  // 淡红色（极度高估）
                    ),
                    startX = 0f,
                    endX = width
                ),
                size = Size(width, height),
                cornerRadius = CornerRadius(height / 2, height / 2)
            )
            
            // 绘制当前值指示器（小圆点加白边）
            val indicatorX = (value / 100 * width).toFloat().coerceIn(0f, width)
            // 外圈白色
            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx(),
                center = Offset(indicatorX, height / 2)
            )
            // 内圈深色
            drawCircle(
                color = Color(0xFF424242),
                radius = 5.dp.toPx(),
                center = Offset(indicatorX, height / 2)
            )
        }
        
        // 刻度标签
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("20", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("40", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("60", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("80", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("100", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * 桑基图页面
 */
@Composable
private fun SankeyChartScreen(
    positionViewModel: com.example.jjsj.viewmodel.PositionViewModel,
    fundViewModel: com.example.jjsj.viewmodel.FundViewModel
) {
    // 获取持仓数据
    val aggregatedPositions by positionViewModel.aggregatedPositions.collectAsState()
    
    // 为每个基金加载详情（包括持仓股）
    LaunchedEffect(aggregatedPositions) {
        aggregatedPositions.forEach { position ->
            fundViewModel.loadFundDetailInfo(position.fundCode)
        }
    }
    
    // 获取所有基金详情状态
    val fundDetailStates = aggregatedPositions.associateWith { position ->
        fundViewModel.fundDetailInfoState.collectAsState().value
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "持仓穿透桑基图",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        if (aggregatedPositions.isEmpty()) {
            // 空状态
            EmptyStateCard()
        } else {
            // 桑基图可视化
            SankeyVisualization(
                positions = aggregatedPositions,
                fundViewModel = fundViewModel
            )
        }
    }
}

/**
 * 空状态卡片
 */
@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "暂无持仓数据",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "请先在持仓管理中添加基金持仓",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 桑基图可视化
 */
@Composable
private fun SankeyVisualization(
    positions: List<com.example.jjsj.data.model.AggregatedPosition>,
    fundViewModel: com.example.jjsj.viewmodel.FundViewModel
) {
    val fundDetailInfoState by fundViewModel.fundDetailInfoState.collectAsState()
    
    // 预定义的颜色列表
    val fundColors = listOf(
        Color(0xFF1976D2), Color(0xFFE53935), Color(0xFF43A047),
        Color(0xFFFDD835), Color(0xFF8E24AA), Color(0xFFFF6F00),
        Color(0xFF00ACC1), Color(0xFF5E35B1), Color(0xFFC62828)
    )
    
    // 计算总金额
    val totalValue = positions.sumOf { it.currentValue }
    
    // 总览卡片
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "持仓总览",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "总市值",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "¥${String.format("%.2f", totalValue)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "持仓基金数",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${positions.size}只",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
    
    // 基金层可视化
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "基金分布",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // 堆叠条形图
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                val width = size.width
                val height = size.height
                var currentX = 0f
                
                positions.forEachIndexed { index, position ->
                    val segmentWidth = (position.currentValue / totalValue * width).toFloat()
                    val color = fundColors[index % fundColors.size]
                    
                    drawRect(
                        color = color,
                        topLeft = Offset(currentX, 0f),
                        size = Size(segmentWidth, height)
                    )
                    
                    currentX += segmentWidth
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 基金列表
            positions.forEachIndexed { index, position ->
                val color = fundColors[index % fundColors.size]
                val percentage = (position.currentValue / totalValue * 100)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.size(16.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(color = color)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = position.fundName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${String.format("%.1f", percentage)}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "¥${String.format("%.2f", position.currentValue)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// 工具函数
private fun getValuationColorByPercentile(percentile: Double): Color {
    return when {
        percentile < 20 -> Color(0xFF66BB6A) // 淡绿色（极度低估）
        percentile < 40 -> Color(0xFF81C784) // 浅绿色（低估）
        percentile < 60 -> Color(0xFFFFD54F) // 淡黄色（正常）
        percentile < 80 -> Color(0xFFFF8A65) // 淡橙色（高估）
        else -> Color(0xFFE57373)           // 淡红色（极度高估）
    }
}

