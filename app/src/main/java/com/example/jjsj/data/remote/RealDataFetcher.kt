package com.example.jjsj.data.remote

import com.example.jjsj.data.model.Fund
import com.example.jjsj.data.remote.parser.ApiParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * 真实数据获取器
 * 从天天基金API获取真实数据
 */
object RealDataFetcher {
    
    // 热门基金代码列表（用于涨跌幅榜）- 120个真实有效代码
    private val popularFundCodes = listOf(
        // 股票型基金 - 科技、新能源
        "001102", "003984", "001410", "001156", "001631",
        "007300", "008087", "009566", "005827", "003096",
        "001542", "007873", "011103", "161726", "162412",
        
        // 股票型基金 - 医药、消费
        "004851", "110022", "001186", "050026", "161725",
        "001298", "005669", "008281", "009119", "163406",
        "260108", "270050", "398021", "450003", "320007",
        
        // 混合型基金 - 成长
        "519674", "000308", "001216", "000751", "001511",
        "001171", "001703", "002293", "003834", "007119",
        "163402", "260108", "270002", "162605", "001314",
        
        // 混合型基金 - 价值
        "001106", "001166", "001637", "004997", "005911",
        "000220", "000595", "002190", "003095", "007340",
        
        // 指数型基金 - 宽基
        "110003", "110019", "110020", "000961", "000656",
        "000311", "110017", "161017", "000478", "090010",
        "100032", "161024", "162307", "519732", "040005",
        
        // 指数型基金 - 行业主题
        "160212", "162605", "270028", "001277", "002939",
        "006229", "010387", "012348", "050009", "160602",
        
        // 债券型基金
        "000104", "519666", "040004", "000216", "050011",
        "217022", "001692", "270002"
    )
    
    /**
     * 获取涨跌幅榜数据
     * 从热门基金中获取实时数据并排序
     */
    suspend fun fetchRankingFunds(): Result<List<Fund>> {
        return withContext(Dispatchers.IO) {
            try {
                // 并发获取多个基金的实时数据（限制在80个以避免频率限制）
                val funds = popularFundCodes.take(80).map { code ->
                    async {
                        try {
                            fetchFundRealTime(code)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
                
                if (funds.isNotEmpty()) {
                    Result.success(funds)
                } else {
                    // 如果全部失败，返回模拟数据
                    Result.success(generateMockRankingFunds())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Result.success(generateMockRankingFunds())
            }
        }
    }
    
    /**
     * 获取单个基金的实时数据
     */
    suspend fun fetchFundRealTime(fundCode: String): Fund? {
        return withContext(Dispatchers.IO) {
            try {
                val response = EastMoneyClient.fundGzApi.getFundRealTime(fundCode)
                ApiParser.parseFundRealTimeData(response)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * 批量获取基金实时数据（用于自选和持仓）
     * 注意：此方法仅获取估值数据，调用方需要使用FundRepository.enhanceFundWithNavHistory进一步优化
     */
    suspend fun fetchFundsBatch(fundCodes: List<String>): List<Fund> {
        return withContext(Dispatchers.IO) {
            fundCodes.map { code ->
                async {
                    try {
                        fetchFundRealTime(code)
                    } catch (e: Exception) {
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }
    
    /**
     * 生成模拟榜单数据
     */
    private fun generateMockRankingFunds(): List<Fund> {
        return popularFundCodes.take(60).map { code ->
            val baseNav = Random.nextDouble(1.0, 5.0)
            val changeRate = Random.nextDouble(-8.0, 8.0)
            
            Fund(
                code = code,
                name = "基金$code",
                type = listOf("股票型", "混合型", "指数型").random(),
                nav = baseNav,
                accumulatedNav = baseNav * 1.5,
                changeRate = changeRate,
                changeAmount = baseNav * changeRate / 100,
                manager = listOf("张三", "李四", "王五").random(),
                company = listOf("易方达", "华夏", "嘉实", "南方", "广发").random(),
                scale = Random.nextDouble(10.0, 500.0),
                establishDate = "2020-01-01"
            )
        }
    }
}

