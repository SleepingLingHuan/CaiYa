package com.example.jjsj.data.model

import kotlinx.serialization.Serializable

/**
 * 指数数据模型
 */
@Serializable
data class IndexData(
    val code: String,
    val name: String,
    val currentPrice: Double,
    val changeAmount: Double,
    val changeRate: Double,
    val updateTime: String = ""
)

/**
 * 预定义的主要指数（仅包含国内指数）
 */
object MajorIndices {
    val domesticIndices = listOf(
        IndexInfo("000001", "上证指数"),
        IndexInfo("399001", "深圳成指"),
        IndexInfo("399006", "创业板指"),
        IndexInfo("000688", "科创50"),
        IndexInfo("000300", "沪深300"),
        IndexInfo("000905", "中证500")
    )
    
    val globalIndices = listOf(
        IndexInfo("hkHSI", "恒生指数"),
        IndexInfo("hkHSTECH", "恒生科技"),
        IndexInfo("int_sp500", "标普500"),
        IndexInfo("int_dji", "道琼斯"),
        IndexInfo("int_nasdaq", "纳斯达克")
    )
    
    // 保持向后兼容
    val indices = domesticIndices
}

data class IndexInfo(
    val code: String,
    val name: String
)

