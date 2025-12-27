package com.example.jjsj.data.remote.parser

import com.example.jjsj.data.local.entity.FundDetailCacheEntity
import com.example.jjsj.data.local.entity.StockHolding
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * 基金详情HTML解析器
 * 从东方财富网站解析基金详细信息
 */
object FundDetailParser {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * 从HTML解析基金详情
     * @param html HTML内容
     * @param fundCode 基金代码
     * @return 基金详情缓存实体
     */
    fun parseFundDetail(html: String, fundCode: String): FundDetailCacheEntity? {
        return try {
            println("🔧 [Parser] 开始解析HTML，基金代码: $fundCode")
            val doc: Document = Jsoup.parse(html)
            println("🔧 [Parser] Jsoup解析完成")
            
            // 解析基本信息表格
            println("🔧 [Parser] 开始解析基本信息表格...")
            val basicInfo = parseBasicInfoTable(doc)
            println("🔧 [Parser] 基本信息解析完成，字段数量: ${basicInfo.size}")
            basicInfo.forEach { (key, value) ->
                println("   - $key: $value")
            }
            
            // 解析投资信息
            println("🔧 [Parser] 开始解析投资信息...")
            val investmentInfo = parseInvestmentInfo(doc)
            println("🔧 [Parser] 投资信息解析完成，字段数量: ${investmentInfo.size}")
            investmentInfo.forEach { (key, value) ->
                println("   - $key: ${value.take(50)}...")
            }
            
            // 持仓信息需要单独API获取，这里返回空列表
            val holdings = emptyList<StockHolding>()
            
            println("🔧 [Parser] 创建FundDetailCacheEntity对象...")
            FundDetailCacheEntity(
                fundCode = fundCode,
                fundName = basicInfo["fundName"] ?: "",
                fundFullName = basicInfo["fundFullName"] ?: "",
                fundType = basicInfo["fundType"] ?: "",
                establishDate = basicInfo["establishDate"] ?: "",
                establishScale = basicInfo["establishScale"] ?: "",
                assetScale = basicInfo["assetScale"] ?: "",
                shareScale = basicInfo["shareScale"] ?: "",
                fundManager = basicInfo["fundManager"] ?: "",
                fundCompany = basicInfo["fundCompany"] ?: "",
                fundCustodian = basicInfo["fundCustodian"] ?: "",
                managementFeeRate = basicInfo["managementFeeRate"] ?: "",
                custodianFeeRate = basicInfo["custodianFeeRate"] ?: "",
                maxSubscriptionFee = basicInfo["maxSubscriptionFee"] ?: "",
                maxPurchaseFee = basicInfo["maxPurchaseFee"] ?: "",
                maxRedemptionFee = basicInfo["maxRedemptionFee"] ?: "",
                investmentObjective = investmentInfo["investmentObjective"] ?: "",
                investmentPhilosophy = investmentInfo["investmentPhilosophy"] ?: "",
                investmentScope = investmentInfo["investmentScope"] ?: "",
                investmentStrategy = investmentInfo["investmentStrategy"] ?: "",
                dividendPolicy = investmentInfo["dividendPolicy"] ?: "",
                riskReturnCharacter = investmentInfo["riskReturnCharacter"] ?: "",
                performanceBenchmark = basicInfo["performanceBenchmark"] ?: "",
                accumulatedDividend = basicInfo["accumulatedDividend"] ?: "",
                topHoldingsJson = json.encodeToString(holdings),
                cacheTime = System.currentTimeMillis()
            ).also {
                println("✅ [Parser] FundDetailCacheEntity创建成功")
            }
        } catch (e: Exception) {
            println("❌ [Parser] 解析异常: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 解析基本信息表格
     */
    private fun parseBasicInfoTable(doc: Document): Map<String, String> {
        val result = mutableMapOf<String, String>()
        
        try {
            println("🔍 [Parser] 查找基金简称...")
            // 解析基金简称
            doc.select(".fundDetail-tit").firstOrNull()?.let {
                result["fundName"] = it.text().trim()
                println("   找到基金简称: ${result["fundName"]}")
            }
            
            println("🔍 [Parser] 查找表格...")
            // 解析表格信息 - 找到class="info"的表格
            val table = doc.select("table.info").firstOrNull()
            if (table == null) {
                println("   ⚠️ 未找到table.info元素")
                return result
            }
            println("   ✅ 找到table.info元素")
            
            val rows = table.select("tr")
            println("   表格行数: ${rows.size}")
            
            rows.forEachIndexed { index, row ->
                // 每行是 th+td+th+td 结构
                val headers = row.select("th")
                val cells = row.select("td")
                println("   行 $index: ${headers.size} 个th, ${cells.size} 个td")
                
                // 处理每对 th+td
                for (i in headers.indices) {
                    val label = headers.getOrNull(i)?.text()?.trim() ?: continue
                    val value = cells.getOrNull(i)?.text()?.trim() ?: continue
                    println("   解析字段: [$label] = [$value]")
                    
                    when {
                        label.contains("基金全称") -> result["fundFullName"] = value
                        label.contains("基金简称") -> result["fundName"] = value
                        label.contains("基金代码") -> {
                            // 提取代码，可能包含前端/后端
                            val code = value.substringBefore("（").substringBefore("(").trim()
                            result["fundCode"] = code
                        }
                        label.contains("基金类型") -> result["fundType"] = value
                        label.contains("成立日期/规模") -> {
                            val parts = value.split("/")
                            if (parts.size >= 2) {
                                result["establishDate"] = parts[0].trim()
                                result["establishScale"] = parts[1].trim()
                            }
                        }
                        label.contains("资产规模") -> result["assetScale"] = value
                        label.contains("份额规模") -> result["shareScale"] = value
                        label.contains("基金管理人") -> result["fundCompany"] = value
                        label.contains("基金托管人") -> result["fundCustodian"] = value
                        label.contains("基金经理人") -> result["fundManager"] = value
                        label.contains("成立来分红") -> result["accumulatedDividend"] = value
                        label.contains("管理费率") -> result["managementFeeRate"] = value
                        label.contains("托管费率") -> result["custodianFeeRate"] = value
                        label.contains("最高认购费率") -> result["maxSubscriptionFee"] = value
                        label.contains("最高申购费率") -> {
                            // 去掉优惠费率部分
                            result["maxPurchaseFee"] = value.substringBefore("天天").trim()
                        }
                        label.contains("最高赎回费率") -> result["maxRedemptionFee"] = value
                        label.contains("业绩比较基准") -> result["performanceBenchmark"] = value
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ [Parser] parseBasicInfoTable异常: ${e.message}")
            e.printStackTrace()
        }
        
        println("🔍 [Parser] parseBasicInfoTable完成，结果: $result")
        return result
    }
    
    /**
     * 解析投资信息
     */
    private fun parseInvestmentInfo(doc: Document): Map<String, String> {
        val result = mutableMapOf<String, String>()
        
        try {
            println("🔍 [Parser] 查找投资信息 h4 标题...")
            val headers = doc.select("h4")
            println("   找到 ${headers.size} 个 h4 元素")
            
            // 查找包含"投资目标"、"投资理念"等标题的元素
            headers.forEachIndexed { index, header ->
                val title = header.text().trim()
                
                // 内容在h4后面的<p>标签里
                var content = ""
                var sibling = header.nextElementSibling()
                while (sibling != null && sibling.tagName() != "h4") {
                    if (sibling.tagName() == "p") {
                        content += sibling.text().trim() + " "
                    }
                    sibling = sibling.nextElementSibling()
                }
                content = content.trim()
                
                println("   h4[$index]: 标题='$title', 内容长度=${content.length}")
                
                when {
                    title.contains("投资目标") -> result["investmentObjective"] = content
                    title.contains("投资理念") -> result["investmentPhilosophy"] = content
                    title.contains("投资范围") -> result["investmentScope"] = content
                    title.contains("投资策略") -> result["investmentStrategy"] = content
                    title.contains("分红政策") -> result["dividendPolicy"] = content
                    title.contains("风险收益特征") -> result["riskReturnCharacter"] = content
                }
            }
        } catch (e: Exception) {
            println("❌ [Parser] parseInvestmentInfo异常: ${e.message}")
            e.printStackTrace()
        }
        
        println("🔍 [Parser] parseInvestmentInfo完成，结果: ${result.keys}")
        return result
    }
    
    /**
     * 解析基金持仓股数据
     * 从FundArchivesDatas.aspx API返回的HTML中提取持仓股信息
     */
    fun parseHoldings(html: String): List<StockHolding> {
        return try {
            println("🔧 [Parser] 开始解析持仓股数据")
            
            // 1. 提取apidata.content
            val contentRegex = """var apidata=\{\s*content:"([^"]*(?:\\.[^"]*)*)"""".toRegex(RegexOption.DOT_MATCHES_ALL)
            val contentMatch = contentRegex.find(html)
            
            if (contentMatch == null) {
                println("❌ [Parser] 未找到apidata.content")
                return emptyList()
            }
            
            val content = contentMatch.groupValues[1]
                .replace("\\r\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\/", "/")
            
            println("✓ [Parser] 找到apidata.content，长度: ${content.length}")
            
            // 2. 解析HTML表格
            val doc = Jsoup.parse(content)
            val rows = doc.select("tr")
            println("✓ [Parser] 找到 ${rows.size} 行数据")
            
            val holdings = mutableListOf<StockHolding>()
            var count = 0
            
            for (row in rows) {
                if (count >= 10) break  // 只取前10条（最新季度数据）
                
                val cells = row.select("td")
                if (cells.size >= 3) {
                    val stockCode = cells[1].text().trim()
                    val stockName = cells[2].text().trim()
                    
                    // 查找占净值比例（包含%的单元格）
                    var holdingRatio = ""
                    for (i in 3 until cells.size) {
                        val text = cells[i].text().trim()
                        if (text.contains("%") && text.firstOrNull()?.isDigit() == true) {
                            holdingRatio = text
                            break
                        }
                    }
                    
                    if (stockCode.isNotEmpty() && stockName.isNotEmpty() && holdingRatio.isNotEmpty()) {
                        holdings.add(
                            StockHolding(
                                stockCode = stockCode,
                                stockName = stockName,
                                holdingRatio = holdingRatio
                            )
                        )
                        println("  ${count + 1}. $stockCode $stockName - $holdingRatio")
                        count++
                    }
                }
            }
            
            println("✓ [Parser] 成功解析 ${holdings.size} 只持仓股")
            holdings
            
        } catch (e: Exception) {
            println("❌ [Parser] 解析持仓股异常: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
}

