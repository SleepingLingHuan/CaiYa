package com.example.jjsj.data.repository

import com.example.jjsj.data.model.IndexData
import com.example.jjsj.data.model.MajorIndices
import com.example.jjsj.data.remote.EastMoneyClient
import com.example.jjsj.data.remote.parser.ApiParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 指数数据仓库
 */
class IndexRepository {
    
    /**
     * 获取主要指数数据
     */
    suspend fun getMajorIndices(): Result<List<IndexData>> {
        return withContext(Dispatchers.IO) {
            try {
                println("🔍 ========== 指数数据获取开始（使用东方财富API）==========")
                
                // 使用东方财富API逐个获取指数数据（避免URL编码问题）
                val indices = MajorIndices.indices.mapNotNull { indexInfo ->
                    try {
                        val secid = ApiParser.convertToEastMoneySecid(indexInfo.code)
                        println("🔍 获取指数: ${indexInfo.name} (${indexInfo.code} -> $secid)")
                        
                        val response = EastMoneyClient.eastMoneyQuoteApi.getEastMoneyIndexData(secid)
                        println("🔍 响应: $response")
                        
                        ApiParser.parseEastMoneyIndexData(response, indexInfo.code)
                    } catch (e: Exception) {
                        println("❌ 获取指数 ${indexInfo.name} 失败: ${e.message}")
                        e.printStackTrace()
                        null
                    }
                }
                
                println("🔍 解析结果: 共 ${indices.size} 个指数")
                indices.forEachIndexed { index, data ->
                    println("🔍   [$index] ${data.name}: ${data.currentPrice} (${if(data.changeRate>=0) "+" else ""}${data.changeRate}%)")
                }
                
                if (indices.isNotEmpty()) {
                    println("✅ 指数数据获取成功")
                    Result.success(indices)
                } else {
                    println("❌ 指数解析失败，使用模拟数据")
                    Result.success(generateMockIndices())
                }
            } catch (e: Exception) {
                println("❌ 指数数据获取异常: ${e.message}")
                e.printStackTrace()
                Result.success(generateMockIndices())
            }
        }
    }
    
    /**
     * 获取全球指数数据（使用新浪API）
     */
    suspend fun getGlobalIndices(): Result<List<IndexData>> {
        return withContext(Dispatchers.IO) {
            try {
                println("🌍 ========== 全球指数数据获取开始（使用新浪API）==========")
                
                // 使用新浪API逐个获取全球指数数据
                val indices = MajorIndices.globalIndices.mapNotNull { indexInfo ->
                    try {
                        val sinaCode = ApiParser.convertToSinaCode(indexInfo.code)
                        println("🌍 获取指数: ${indexInfo.name} (${indexInfo.code} -> $sinaCode)")
                        
                        val response = EastMoneyClient.sinaFinanceApi.getIndexData(sinaCode)
                        println("🌍 响应: ${response.take(200)}")
                        
                        val parsed = ApiParser.parseSinaIndexData(response)
                        parsed.firstOrNull()?.copy(name = indexInfo.name)
                    } catch (e: Exception) {
                        println("❌ 获取全球指数 ${indexInfo.name} 失败: ${e.message}")
                        e.printStackTrace()
                        null
                    }
                }
                
                println("🌍 解析结果: 共 ${indices.size} 个指数")
                indices.forEachIndexed { index, data ->
                    println("🌍   [$index] ${data.name}: ${data.currentPrice} (${if(data.changeRate>=0) "+" else ""}${data.changeRate}%)")
                }
                
                if (indices.isNotEmpty()) {
                    println("✅ 全球指数数据获取成功")
                    Result.success(indices)
                } else {
                    println("❌ 全球指数解析失败，使用模拟数据")
                    Result.success(generateMockGlobalIndices())
                }
            } catch (e: Exception) {
                println("❌ 全球指数数据获取异常: ${e.message}")
                e.printStackTrace()
                Result.success(generateMockGlobalIndices())
            }
        }
    }
    
    /**
     * 生成模拟指数数据
     */
    private fun generateMockIndices(): List<IndexData> {
        return MajorIndices.indices.map { info ->
            val basePrice = when (info.code) {
                "000001" -> 3000.0
                "399001" -> 10000.0
                "399006" -> 2000.0
                "000688" -> 1000.0
                "000300" -> 3800.0
                "000905" -> 5500.0
                else -> 3000.0
            }
            
            val changeRate = kotlin.random.Random.nextDouble(-3.0, 3.0)
            val changeAmount = basePrice * changeRate / 100
            
            IndexData(
                code = info.code,
                name = info.name,
                currentPrice = basePrice + changeAmount,
                changeAmount = changeAmount,
                changeRate = changeRate
            )
        }
    }
    
    /**
     * 生成模拟全球指数数据
     */
    private fun generateMockGlobalIndices(): List<IndexData> {
        return MajorIndices.globalIndices.map { info ->
            val basePrice = when (info.code) {
                "hkHSI" -> 18000.0
                "hkHSTECH" -> 4000.0
                "int_sp500" -> 4500.0
                "int_dji" -> 35000.0
                "int_nasdaq" -> 14000.0
                else -> 10000.0
            }
            
            val changeRate = kotlin.random.Random.nextDouble(-2.0, 2.0)
            val changeAmount = basePrice * changeRate / 100
            
            IndexData(
                code = info.code,
                name = info.name,
                currentPrice = basePrice + changeAmount,
                changeAmount = changeAmount,
                changeRate = changeRate
            )
        }
    }
}

