package com.example.jjsj.data.local

import android.content.Context
import com.example.jjsj.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

/**
 * 基金索引数据加载器
 * 从资源文件加载全国所有公募基金的基本信息
 * 总数: 25234 只
 */
object FundIndexData {
    
    data class FundBasicInfo(
        val code: String,
        val name: String,
        val type: String,
        val pinyin: String
    )
    
    private var cachedFunds: List<FundBasicInfo>? = null
    
    /**
     * 加载基金索引数据
     * 首次调用时从资源文件加载，后续使用缓存
     */
    suspend fun loadFunds(context: Context): List<FundBasicInfo> = withContext(Dispatchers.IO) {
        // 如果已缓存，直接返回
        cachedFunds?.let { return@withContext it }
        
        try {
            // 从raw资源读取JSON文件
            val inputStream = context.resources.openRawResource(R.raw.fund_index)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            
            // 解析JSON数组
            val jsonArray = JSONArray(jsonString)
            val funds = mutableListOf<FundBasicInfo>()
            
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONArray(i)
                funds.add(
                    FundBasicInfo(
                        code = item.getString(0),
                        name = item.getString(1),
                        type = item.getString(2),
                        pinyin = item.getString(3)
                    )
                )
            }
            
            cachedFunds = funds
            funds
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * 搜索基金
     * @param context Android上下文
     * @param keyword 搜索关键词（支持代码、名称、拼音）
     * @return 匹配的基金列表
     */
    suspend fun search(context: Context, keyword: String): List<FundBasicInfo> {
        if (keyword.isBlank()) return emptyList()
        
        val allFunds = loadFunds(context)
        val lowerKeyword = keyword.lowercase()
        
        return allFunds.filter { fund ->
            fund.code.contains(lowerKeyword) ||
            fund.name.lowercase().contains(lowerKeyword) ||
            fund.pinyin.lowercase().contains(lowerKeyword)
        }.take(50) // 限制返回数量
    }
}
