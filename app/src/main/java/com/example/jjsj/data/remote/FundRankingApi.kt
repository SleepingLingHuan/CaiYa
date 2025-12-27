package com.example.jjsj.data.remote

import com.example.jjsj.data.model.Fund
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * 基金排行榜API
 * 从天天基金网获取全部基金的涨跌幅排行榜
 */
object FundRankingApi {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    
    /**
     * 排序类型
     * 参数对照（天天基金网URL中带s前缀，API中不带）：
     * - 日涨跌幅: srzdf → rzdf
     * - 近1周: zzf
     * - 近1月: 1yzf
     * - 近1年: 1nzf
     */
    enum class SortType(val value: String, val description: String) {
        DAY("rzdf", "日涨跌幅"),
        WEEK("zzf", "近1周涨跌幅"),
        MONTH("1yzf", "近1月涨跌幅"),
        YEAR("1nzf", "近1年涨跌幅")
    }
    
    /**
     * 获取基金排行榜
     * @param sortType 排序类型
     * @param page 页码（从1开始）
     * @param pageSize 每页数量（建议50-100）
     * @param ascending 是否升序（false为降序，获取涨幅榜；true为升序，获取跌幅榜）
     * @return 基金列表
     */
    suspend fun fetchRanking(
        sortType: SortType = SortType.DAY,
        page: Int = 1,
        pageSize: Int = 100,
        ascending: Boolean = false
    ): Result<RankingResult> = withContext(Dispatchers.IO) {
        try {
            val url = buildUrl(sortType, page, pageSize, ascending)
            
            val request = Request.Builder()
                .url(url)
                .header("Referer", "https://fund.eastmoney.com/data/fundranking.html")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "*/*")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}"))
            }
            
            val body = response.body?.string() ?: ""
            
            // 解析响应，传入sortType以便选择正确的涨跌幅字段
            val result = parseRankingResponse(body, sortType)
            
            Result.success(result)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * 构建请求URL
     */
    private fun buildUrl(
        sortType: SortType,
        page: Int,
        pageSize: Int,
        ascending: Boolean
    ): String {
        val sortOrder = if (ascending) "asc" else "desc"
        
        return "https://fund.eastmoney.com/data/rankhandler.aspx?" +
                "op=ph&" +                    // 操作类型
                "dt=kf&" +                    // 数据类型（开放式基金）
                "ft=all&" +                   // 基金类型（全部）
                "sc=${sortType.value}&" +     // 排序字段
                "st=$sortOrder&" +            // 排序方向
                "pi=$page&" +                 // 页码
                "pn=$pageSize&" +             // 每页数量
                "fl=0&" +                     // 过滤条件
                "isab=1"                      // 包含AB份额
    }
    
    /**
     * 解析排行榜响应
     * 响应格式：var rankData = {datas:["...", "..."], allRecords:12345, ...};
     * @param response API响应字符串
     * @param sortType 排序类型，用于选择正确的涨跌幅字段
     */
    private fun parseRankingResponse(response: String, sortType: SortType): RankingResult {
        try {
            // 提取总记录数
            val totalRecordsRegex = """allRecords:(\d+)""".toRegex()
            val totalRecordsMatch = totalRecordsRegex.find(response)
            val totalRecords = totalRecordsMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
            
            // 提取数据数组
            val datasRegex = """datas:\[(.*?)\]""".toRegex(RegexOption.DOT_MATCHES_ALL)
            val datasMatch = datasRegex.find(response)
            val datasString = datasMatch?.groupValues?.get(1) ?: ""
            
            // 提取所有基金数据字符串
            val fundItemRegex = """"([^"]+)"""".toRegex()
            val fundItems = fundItemRegex.findAll(datasString).map { it.groupValues[1] }.toList()
            
            // 解析每条基金数据，传入sortType以选择正确的涨跌幅字段
            val funds = fundItems.mapNotNull { parseFundItem(it, sortType) }
            
            // 提取数据时间（从第一条基金的日期字段）
            val dataDate = if (fundItems.isNotEmpty()) {
                val firstItem = fundItems[0].split(",")
                firstItem.getOrNull(3) ?: ""  // 第4个字段是日期
            } else ""
            
            return RankingResult(
                funds = funds,
                totalRecords = totalRecords,
                dataDate = dataDate
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return RankingResult(emptyList(), 0, "")
        }
    }
    
    /**
     * 解析单条基金数据
     * 格式：代码,名称,拼音,日期,单位净值,累计净值,日涨跌幅,近1周,近1月,近3月,近6月,近1年,近3年,成立以来,...
     * @param item 单条基金数据字符串
     * @param sortType 排序类型，用于选择正确的涨跌幅字段
     */
    private fun parseFundItem(item: String, sortType: SortType): Fund? {
        try {
            val fields = item.split(",")
            
            if (fields.size < 12) {
                return null
            }
            
            val code = fields[0]
            val name = fields[1]
            val date = fields.getOrNull(3) ?: ""
            val nav = fields.getOrNull(4)?.toDoubleOrNull() ?: 0.0
            val accumulatedNav = fields.getOrNull(5)?.toDoubleOrNull() ?: 0.0
            
            // 根据sortType选择正确的涨跌幅字段
            val changeRateFieldIndex = when (sortType) {
                SortType.DAY -> 6    // 日涨跌幅
                SortType.WEEK -> 7   // 近1周涨跌幅
                SortType.MONTH -> 8  // 近1月涨跌幅
                SortType.YEAR -> 11  // 近1年涨跌幅
            }
            
            val changeRate = fields.getOrNull(changeRateFieldIndex)?.toDoubleOrNull() ?: 0.0
            
            // 如果没有净值数据，跳过
            if (nav == 0.0 && accumulatedNav == 0.0) {
                return null
            }
            
            return Fund(
                code = code,
                name = name,
                type = "", // 排行榜API不提供类型信息
                nav = nav,
                accumulatedNav = accumulatedNav,
                changeRate = changeRate,  // 使用对应时间维度的涨跌幅
                changeAmount = 0.0,       // 排行榜API不提供涨跌额
                manager = "",
                company = "",
                scale = 0.0,
                establishDate = date
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * 排行榜结果
     */
    data class RankingResult(
        val funds: List<Fund>,
        val totalRecords: Int,
        val dataDate: String  // 数据时间，如"2025-10-20"
    )
}
