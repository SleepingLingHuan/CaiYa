package com.example.jjsj.data.model

/**
 * 估值数据模型
 */
data class ValuationData(
    val indexName: String,      // 指数名称
    val pe: Double,              // 市盈率
    val pePercentile: Double,    // PE百分位
    val pb: Double,              // 市净率
    val pbPercentile: Double     // PB百分位
) {
    /**
     * 综合估值百分位（PE和PB的平均值）
     */
    val compositePercentile: Double
        get() = (pePercentile + pbPercentile) / 2.0
    
    /**
     * 估值等级
     */
    val valuationLevel: ValuationLevel
        get() = when {
            compositePercentile < 20 -> ValuationLevel.EXTREMELY_LOW
            compositePercentile < 40 -> ValuationLevel.LOW
            compositePercentile < 60 -> ValuationLevel.NORMAL
            compositePercentile < 80 -> ValuationLevel.HIGH
            else -> ValuationLevel.EXTREMELY_HIGH
        }
}

/**
 * 估值等级枚举
 */
enum class ValuationLevel(val label: String, val color: Long) {
    EXTREMELY_LOW("极度低估", 0xFF1B5E20),
    LOW("低估", 0xFF388E3C),
    NORMAL("正常", 0xFFFFA000),
    HIGH("高估", 0xFFE64A19),
    EXTREMELY_HIGH("极度高估", 0xFFB71C1C)
}

/**
 * 预定义的估值数据
 */
object ValuationDataSource {
    val valuations = listOf(
        ValuationData("上证指数", 16.79, 99.09, 1.506, 62.06),
        ValuationData("深圳成指", 31.00, 82.46, 2.720, 48.13),
        ValuationData("创业板指", 42.63, 49.57, 5.328, 59.78),
        ValuationData("科创50", 183.63, 98.97, 6.446, 73.78),
        ValuationData("沪深300", 14.40, 89.30, 1.502, 60.15),
        ValuationData("中证500", 34.37, 82.50, 2.287, 73.98),
        ValuationData("恒生指数", 11.91, 79.50, 1.265, 83.94),
        ValuationData("恒生科技", 23.53, 31.68, 3.411, 71.66),
        ValuationData("纳斯达克", 34.10, 75.21, 4.412, 73.90)
    )
}

