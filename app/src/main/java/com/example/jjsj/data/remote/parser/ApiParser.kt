package com.example.jjsj.data.remote.parser

import com.example.jjsj.data.model.Fund
import com.example.jjsj.data.model.IndexData
import org.json.JSONObject

/**
 * API数据解析器
 */
object ApiParser {
    
    /**
     * 解析天天基金实时估值数据
     * 格式: jsonpgz({"fundcode":"000001","name":"华夏成长","jzrq":"2023-10-13","dwjz":"1.2345","gsz":"1.2400","gszzl":"0.45","gztime":"2023-10-13 15:00"});
     */
    fun parseFundRealTimeData(jsonpString: String): Fund? {
        try {
            // 移除JSONP包装: jsonpgz(...);
            val jsonString = jsonpString
                .removePrefix("jsonpgz(")
                .removeSuffix(");")
                .trim()
            
            val json = JSONObject(jsonString)
            
            val fundCode = json.getString("fundcode")
            val fundName = json.getString("name")
            val jzrq = json.optString("jzrq", "")  // 净值日期
            val dwjz = json.optString("dwjz", "0").toDoubleOrNull() ?: 0.0  // 单位净值
            val gsz = json.optString("gsz", "0").toDoubleOrNull() ?: 0.0    // 估算净值
            val changeRate = json.optString("gszzl", "0").toDoubleOrNull() ?: 0.0  // 估算涨跌幅
            val gztime = json.optString("gztime", "")  // 估值时间
            
            return Fund(
                code = fundCode,
                name = fundName,
                type = "未知",
                nav = if (gsz > 0) gsz else dwjz,  // 优先使用估算净值
                accumulatedNav = dwjz,
                changeRate = changeRate,
                changeAmount = (gsz - dwjz),
                manager = "",
                company = "",
                scale = 0.0,
                establishDate = jzrq
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * 解析新浪财经指数数据
     * 格式1(国内): var hq_str_s_sh000001="上证指数,3088.8039,23.6001,0.77,1041143,13323944";
     * 格式2(恒生): var hq_str_rt_hkHSI="HSI,恒生指数,25890.010,25910.600,26062.800,25687.320,25836.270,-74.330,-0.290,...";
     * 格式3(国际): var hq_str_int_sp500="标普指数,6643.70,38.98,0.59";
     */
    fun parseSinaIndexData(responseString: String): List<IndexData> {
        val indices = mutableListOf<IndexData>()
        
        try {
            // 按行分割
            val lines = responseString.split("\n")
            
            for (line in lines) {
                if (line.contains("hq_str_")) {
                    // 提取代码和数据
                    val regex = """var hq_str_(.+?)="(.+?)";""".toRegex()
                    val matchResult = regex.find(line) ?: continue
                    
                    val code = matchResult.groupValues[1]
                    val data = matchResult.groupValues[2]
                    
                    // 如果数据为空，跳过
                    if (data.isEmpty()) continue
                    
                    val fields = data.split(",")
                    
                    // 根据不同格式解析
                    val (name, currentPrice, changeAmount, changeRate) = when {
                        // 恒生指数格式: 代码,名称,现价,开盘,最高,最低,昨收,涨跌额,涨跌幅,...
                        code.startsWith("rt_hk") && fields.size >= 9 -> {
                            val indexName = fields[1]
                            val price = fields[6].toDoubleOrNull() ?: 0.0  // 昨收+涨跌额=现价
                            val change = fields[7].toDoubleOrNull() ?: 0.0
                            val rate = fields[8].toDoubleOrNull() ?: 0.0
                            listOf(indexName, price, change, rate)
                        }
                        // 国际指数格式: 名称,现价,涨跌额,涨跌幅
                        code.startsWith("int_") && fields.size >= 4 -> {
                            val indexName = fields[0]
                            val price = fields[1].toDoubleOrNull() ?: 0.0
                            val change = fields[2].toDoubleOrNull() ?: 0.0
                            val rate = fields[3].toDoubleOrNull() ?: 0.0
                            listOf(indexName, price, change, rate)
                        }
                        // 国内指数格式: 名称,现价,涨跌额,涨跌幅,...
                        fields.size >= 4 -> {
                            val indexName = fields[0]
                            val price = fields[1].toDoubleOrNull() ?: 0.0
                            val change = fields[2].toDoubleOrNull() ?: 0.0
                            val rate = fields[3].toDoubleOrNull() ?: 0.0
                            listOf(indexName, price, change, rate)
                        }
                        else -> continue
                    }
                    
                    indices.add(
                        IndexData(
                            code = code,
                            name = name as String,
                            currentPrice = currentPrice as Double,
                            changeAmount = changeAmount as Double,
                            changeRate = changeRate as Double
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return indices
    }
    
    /**
     * 将指数代码转换为新浪财经格式
     * 例如: 000001 -> s_sh000001, 399001 -> s_sz399001
     * 全球指数: hkHSI -> rt_hkHSI, int_dji -> int_dji
     */
    fun convertToSinaCode(code: String): String {
        return when {
            code.startsWith("000") || code.startsWith("60") -> "s_sh$code"
            code.startsWith("399") || code.startsWith("30") -> "s_sz$code"
            code == "HSI" -> "rt_hkHSI"
            code == "HSTECH" -> "rt_hkHSTECH"
            code == "IXIC" -> "gb_$code"
            code.startsWith("hk") -> "rt_$code"  // 香港指数
            code.startsWith("int_") -> code      // 国际指数
            else -> "s_sh$code"
        }
    }
    
    /**
     * 将指数代码转换为东方财富格式
     * 例如: 000001 -> 1.000001 (上证指数-上海), 399001 -> 0.399001 (深证成指-深圳)
     */
    fun convertToEastMoneySecid(code: String): String {
        return when {
            // 深圳市场指数（399开头）
            code.startsWith("399") -> "0.$code"
            
            // 上海市场指数（000、688开头的主要指数）
            // 000001=上证指数, 000300=沪深300, 000688=科创50, 000905=中证500
            code in listOf("000001", "000300", "000688", "000905", "000016", "000852") -> "1.$code"
            
            // 其他000开头可能是深圳（但主要指数都在上面列出了）
            code.startsWith("000") -> "1.$code"  // 默认上海
            
            // 默认上海市场
            else -> "1.$code"
        }
    }
    
    /**
     * 解析东方财富指数数据
     * 格式: {"rc":0,"data":{"f43":388950,"f58":"上证指数","f169":-753,"f170":-19}}
     * 注意：数值需要除以100（f43, f169）或100（f170）
     */
    fun parseEastMoneyIndexData(jsonString: String, originalCode: String): IndexData? {
        return try {
            println("🔍 [东方财富] 解析指数数据: $originalCode")
            println("🔍 [东方财富] 原始JSON: $jsonString")
            
            val json = JSONObject(jsonString)
            val rc = json.optInt("rc", -1)
            
            if (rc != 0) {
                println("❌ [东方财富] rc != 0, rc=$rc")
                return null
            }
            
            val data = json.optJSONObject("data")
            if (data == null) {
                println("❌ [东方财富] data字段为空")
                return null
            }
            
            val name = data.optString("f58", "")
            // 东方财富返回的数值需要除以100
            val currentPrice = data.optDouble("f43", 0.0) / 100.0  // 388950 -> 3889.50
            val changeAmount = data.optDouble("f169", 0.0) / 100.0  // -753 -> -7.53
            val changeRate = data.optDouble("f170", 0.0) / 100.0    // -19 -> -0.19
            
            println("✅ [东方财富] 解析成功: $name = ${"%.2f".format(currentPrice)} (${if(changeRate>=0) "+" else ""}${"%.2f".format(changeRate)}%)")
            
            IndexData(
                code = originalCode,
                name = name,
                currentPrice = currentPrice,
                changeAmount = changeAmount,
                changeRate = changeRate
            )
        } catch (e: Exception) {
            println("❌ [东方财富] 解析异常: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}

