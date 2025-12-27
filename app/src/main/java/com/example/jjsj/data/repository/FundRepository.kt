package com.example.jjsj.data.repository

import android.content.Context
import com.example.jjsj.data.local.dao.FavoriteFundDao
import com.example.jjsj.data.local.dao.FundCacheDao
import com.example.jjsj.data.local.dao.FundDetailCacheDao
import com.example.jjsj.data.local.dao.FundNavCacheDao
import com.example.jjsj.data.local.entity.FavoriteFundEntity
import com.example.jjsj.data.local.entity.FundCacheEntity
import com.example.jjsj.data.local.entity.FundDetailCacheEntity
import com.example.jjsj.data.local.entity.FundNavCacheEntity
import com.example.jjsj.data.local.entity.FundNavItemCache
import com.example.jjsj.data.local.entity.StockHolding
import com.example.jjsj.data.local.FundIndexData
import com.example.jjsj.data.model.Fund
import com.example.jjsj.data.model.FundDetail
import com.example.jjsj.data.model.FundNav
import com.example.jjsj.data.remote.RealDataFetcher
import com.example.jjsj.data.remote.EastMoneyClient
import com.example.jjsj.data.remote.FundRankingApi
import com.example.jjsj.data.remote.parser.FundDetailParser
import com.example.jjsj.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * 基金数据仓库
 * 负责协调本地数据库和网络数据
 */
class FundRepository(
    private val context: Context,
    private val fundCacheDao: FundCacheDao,
    private val favoriteFundDao: FavoriteFundDao,
    private val fundDetailCacheDao: FundDetailCacheDao,
    private val fundNavCacheDao: FundNavCacheDao
) {
    
    /**
     * 获取涨幅榜（带数据时间）
     * 从天天基金网排行榜API获取涨跌幅数据（降序排列）
     * @return Pair<基金列表, 数据时间>
     */
    suspend fun getTopGainersWithDate(
        forceRefresh: Boolean = false,
        sortType: FundRankingApi.SortType = FundRankingApi.SortType.DAY
    ): Result<Pair<List<Fund>, String>> {
        return fetchRankingWithDate(sortType, ascending = false, rankingType = "涨幅榜")
    }
    
    /**
     * 获取涨幅榜（兼容旧接口）
     */
    suspend fun getTopGainers(
        forceRefresh: Boolean = false,
        sortType: FundRankingApi.SortType = FundRankingApi.SortType.DAY
    ): Result<List<Fund>> {
        return fetchRanking(sortType, ascending = false, rankingType = "涨幅榜")
    }
    
    /**
     * 获取跌幅榜
     * 从天天基金网排行榜API获取涨跌幅数据（升序排列）
     * @param forceRefresh 是否强制刷新
     * @param sortType 排序类型（默认按日涨跌幅）
     */
    suspend fun getTopLosers(
        forceRefresh: Boolean = false,
        sortType: FundRankingApi.SortType = FundRankingApi.SortType.DAY
    ): Result<List<Fund>> {
        return fetchRanking(sortType, ascending = true, rankingType = "跌幅榜")
    }
    
    /**
     * 获取基金列表（涨跌幅榜数据）- 保留用于兼容
     */
    suspend fun getFundList(
        forceRefresh: Boolean = false,
        sortType: FundRankingApi.SortType = FundRankingApi.SortType.DAY
    ): Result<List<Fund>> {
        return getTopGainers(forceRefresh, sortType)
    }
    
    /**
     * 通用的排行榜获取方法（带数据时间）
     */
    private suspend fun fetchRankingWithDate(
        sortType: FundRankingApi.SortType,
        ascending: Boolean,
        rankingType: String
    ): Result<Pair<List<Fund>, String>> {
        return withContext(Dispatchers.IO) {
            try {
                val sortOrder = if (ascending) "升序" else "降序"
                println("📊 [Ranking] 开始获取${rankingType}数据: sortType=$sortType, 排序=$sortOrder")
                
                // 从排行榜API获取前50条数据
                val rankingResult = FundRankingApi.fetchRanking(
                    sortType = sortType,
                    page = 1,
                    pageSize = 50,
                    ascending = ascending
                )
                
                rankingResult.onSuccess { result ->
                    println("✅ [Ranking] ${rankingType}获取成功: ${result.funds.size}/${result.totalRecords} 条记录, 数据时间=${result.dataDate}")
                    
                    val funds = result.funds
                    val dataDate = result.dataDate
                    
                    // 缓存基本信息到本地
                    val cacheEntities = funds.map { it.toEntity() }
                    fundCacheDao.insertAll(cacheEntities)
                    
                    return@withContext Result.success(Pair(funds, dataDate))
                }
                
                rankingResult.onFailure { error ->
                    println("❌ [Ranking] ${rankingType}获取失败: ${error.message}")
                    return@withContext Result.failure(error)
                }
                
                Result.success(Pair(emptyList(), ""))
            } catch (e: Exception) {
                e.printStackTrace()
                println("❌ [Ranking] ${rankingType}异常: ${e.message}")
                Result.failure(e)
            }
        }
    }
    
    /**
     * 通用的排行榜获取方法
     */
    private suspend fun fetchRanking(
        sortType: FundRankingApi.SortType,
        ascending: Boolean,
        rankingType: String
    ): Result<List<Fund>> {
        return withContext(Dispatchers.IO) {
            try {
                val sortOrder = if (ascending) "升序" else "降序"
                println("📊 [Ranking] 开始获取${rankingType}数据: sortType=$sortType, 排序=$sortOrder")
                
                // 从排行榜API获取前50条数据
                val rankingResult = FundRankingApi.fetchRanking(
                    sortType = sortType,
                    page = 1,
                    pageSize = 50,
                    ascending = ascending
                )
                
                rankingResult.onSuccess { result ->
                    println("✅ [Ranking] ${rankingType}获取成功: ${result.funds.size}/${result.totalRecords} 条记录")
                    
                    // 直接使用API返回的数据，不再获取详细信息
                    val funds = result.funds
                    
                    // 缓存基本信息到本地（用于搜索等功能）
                    val cacheEntities = funds.map { it.toEntity() }
                    fundCacheDao.insertAll(cacheEntities)
                    
                    return@withContext Result.success(funds)
                }
                
                rankingResult.onFailure { error ->
                    println("❌ [Ranking] ${rankingType}获取失败: ${error.message}")
                    return@withContext Result.failure(error)
                }
                
                Result.success(emptyList())
            } catch (e: Exception) {
                e.printStackTrace()
                println("❌ [Ranking] ${rankingType}异常: ${e.message}")
                Result.failure(e)
            }
        }
    }
    
    /**
     * 使用净值历史增强基金数据（优先使用净值历史的准确数据）
     * @param fund 原始基金数据（可能来自估值）
     * @return 增强后的基金数据
     */
    suspend fun enhanceFundWithNavHistory(fund: Fund): Fund {
        return try {
            val currentDate = com.example.jjsj.util.DateUtils.getCurrentDate()
            
            // 尝试获取净值历史
            val navHistoryResult = getFundNavHistory(fund.code, pageSize = 2, forceRefresh = false)
            
            navHistoryResult.getOrNull()?.let { navList ->
                if (navList.isNotEmpty()) {
                    val latestNav = navList[0]
                    
                    // 如果最新净值是当日数据，使用净值历史的准确数据
                    if (latestNav.date == currentDate) {
                        println("📊 [Enhance] 使用净值历史数据: ${fund.code} ${latestNav.date}")
                        return fund.copy(
                            nav = latestNav.nav,
                            accumulatedNav = latestNav.accumulatedNav,
                            changeRate = latestNav.changeRate,
                            changeAmount = if (navList.size > 1) {
                                latestNav.nav - navList[1].nav
                            } else {
                                latestNav.nav * (latestNav.changeRate / 100)
                            }
                        )
                    }
                }
            }
            
            // 如果没有当日净值历史，返回原始数据（估值数据）
            fund
        } catch (e: Exception) {
            // 如果出错，返回原始数据
            fund
        }
    }
    
    /**
     * 搜索基金
     * 默认从基金索引库中检索所有基金（全国25234只基金）
     * 返回基本信息，用户点击后再加载详情
     */
    suspend fun searchFunds(keyword: String): Result<List<Fund>> {
        return withContext(Dispatchers.IO) {
            try {
                // 从基金索引搜索（全量搜索）
                val indexMatches = FundIndexData.search(context, keyword)
                
                if (indexMatches.isEmpty()) {
                    return@withContext Result.success(emptyList())
                }
                
                // 转换为Fund对象（只包含基本信息）
                // 如果缓存中有对应基金，使用缓存的完整数据；否则使用基本信息
                val searchResults = indexMatches.map { basicInfo ->
                    val cached = fundCacheDao.getFundByCode(basicInfo.code)
                    if (cached != null) {
                        cached.toFund()
                    } else {
                        // 创建基础Fund对象（只有基本信息）
                        Fund(
                            code = basicInfo.code,
                            name = basicInfo.name,
                            type = basicInfo.type,
                            nav = 0.0,
                            accumulatedNav = 0.0,
                            changeRate = 0.0,
                            changeAmount = 0.0,
                            manager = "",
                            company = "",
                            scale = 0.0,
                            establishDate = ""
                        )
                    }
                }
                
                Result.success(searchResults)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.success(emptyList())
            }
        }
    }
    
    /**
     * 获取基金详情（按需加载）
     * 优先从缓存获取，如果没有则实时获取
     */
    suspend fun getFundDetail(code: String): Result<FundDetail> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. 尝试从缓存获取基金基本信息
                var fund = fundCacheDao.getFundByCode(code)?.toFund()
                
                // 2. 如果缓存中没有，实时获取
                if (fund == null) {
                    println("📋 [Detail] 缓存中没有基金 $code，开始实时获取...")
                    val realTimeFund = RealDataFetcher.fetchFundRealTime(code)
                    
                    if (realTimeFund != null) {
                        // 使用净值历史增强数据
                        fund = enhanceFundWithNavHistory(realTimeFund)
                        // 缓存到本地
                        fundCacheDao.insert(fund.toEntity())
                        println("✅ [Detail] 成功获取并缓存基金数据: ${fund.name}")
                    } else {
                        // 如果实时获取失败，尝试从索引库获取基本信息
                        val allFunds = FundIndexData.loadFunds(context)
                        val indexFund = allFunds.find { it.code == code }
                        
                        if (indexFund != null) {
                            println("⚠️ [Detail] 实时API失败，使用索引库基本信息")
                            fund = Fund(
                                code = indexFund.code,
                                name = indexFund.name,
                                type = indexFund.type,
                                nav = 0.0,
                                accumulatedNav = 0.0,
                                changeRate = 0.0,
                                changeAmount = 0.0,
                                manager = "",
                                company = "",
                                scale = 0.0,
                                establishDate = ""
                            )
                        } else {
                            return@withContext Result.failure(Exception("无法获取基金数据"))
                        }
                    }
                }
                
                // 3. 创建详情对象
                val detail = FundDetail(
                    fund = fund,
                    description = "请查看下方基本概况了解更多信息",
                    investmentStrategy = "请查看下方投资策略了解更多信息",
                    performanceData = com.example.jjsj.data.model.PerformanceData(
                        week = 0.0,
                        month = 0.0,
                        threeMonths = 0.0,
                        sixMonths = 0.0,
                        year = 0.0,
                        threeYears = 0.0,
                        allTime = 0.0
                    ),
                    topHoldings = emptyList()
                )
                Result.success(detail)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }
    
    /**
     * 获取所有自选基金
     * 从天天基金API获取最新数据，优先使用净值历史的准确数据
     */
    fun getFavoriteFunds(): Flow<List<Fund>> {
        return favoriteFundDao.getAllFavorites().map { favorites ->
            if (favorites.isEmpty()) {
                emptyList()
            } else {
                // 批量获取自选基金的实时数据
                val fundCodes = favorites.map { it.fundCode }
                try {
                    // 使用runBlocking在map中调用suspend函数
                    runBlocking {
                        val realFunds = RealDataFetcher.fetchFundsBatch(fundCodes)
                        // 使用净值历史增强数据
                        val accurateFunds = realFunds.map { fund ->
                            enhanceFundWithNavHistory(fund)
                        }
                        // 更新缓存
                        accurateFunds.forEach { fund ->
                            fundCacheDao.insert(fund.toEntity())
                        }
                        accurateFunds
                    }
                } catch (e: Exception) {
                    // 失败时从缓存读取
                    favorites.mapNotNull { favorite ->
                        fundCacheDao.getFundByCode(favorite.fundCode)?.toFund()
                    }
                }
            }
        }
    }
    
    /**
     * 添加自选基金
     */
    suspend fun addFavorite(fund: Fund): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val entity = FavoriteFundEntity(
                    fundCode = fund.code,
                    fundName = fund.name
                )
                favoriteFundDao.insert(entity)
                
                // 同时缓存基金信息
                fundCacheDao.insert(fund.toEntity())
                
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 删除自选基金
     */
    suspend fun removeFavorite(fundCode: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                favoriteFundDao.deleteByCode(fundCode)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 检查是否已收藏
     */
    suspend fun isFavorite(fundCode: String): Boolean {
        return withContext(Dispatchers.IO) {
            favoriteFundDao.isFavorite(fundCode)
        }
    }
    
    /**
     * 清理过期缓存
     */
    suspend fun clearOldCache() {
        withContext(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis() - Constants.CACHE_TIMEOUT
            fundCacheDao.deleteOldCache(timestamp)
        }
    }
    
    /**
     * 获取基金详情
     * 优先从本地缓存获取，如果缓存过期（30天）则从网络获取
     * @param fundCode 基金代码
     * @return 基金详情缓存实体
     */
    suspend fun getFundDetailInfo(fundCode: String): Result<FundDetailCacheEntity?> {
        return withContext(Dispatchers.IO) {
            try {
                println("🔍 ========== 获取基金详情开始: $fundCode ==========")
                
                // 1. 从本地缓存获取
                val cachedDetail = fundDetailCacheDao.getFundDetailCache(fundCode)
                println("📦 缓存查询结果: ${if (cachedDetail != null) "存在" else "不存在"}")
                
                // 2. 检查缓存是否过期
                if (cachedDetail != null && !cachedDetail.isExpired()) {
                    println("✅ 使用缓存数据（未过期）")
                    println("📊 缓存数据: fundName=${cachedDetail.fundName}, fundType=${cachedDetail.fundType}")
                    return@withContext Result.success(cachedDetail)
                }
                
                println("🌐 开始从网络获取HTML...")
                // 3. 缓存不存在或已过期，从网络获取
                val html = try {
                    EastMoneyClient.fundDetailApi.getFundBasicInfo(fundCode)
                } catch (e: Exception) {
                    println("❌ 网络请求失败: ${e.message}")
                    e.printStackTrace()
                    // 网络请求失败，返回旧缓存（如果存在）
                    return@withContext Result.success(cachedDetail)
                }
                
                println("✅ HTML获取成功，长度: ${html.length} 字节")
                println("📄 HTML前200字符: ${html.take(200)}")
                
                // 4. 解析HTML
                println("🔧 开始解析HTML...")
                var fundDetail = FundDetailParser.parseFundDetail(html, fundCode)
                
                if (fundDetail == null) {
                    println("❌ HTML解析失败，返回null")
                } else {
                    println("✅ HTML解析成功！")
                    println("📊 解析结果:")
                    println("   - fundName: ${fundDetail.fundName}")
                    println("   - fundFullName: ${fundDetail.fundFullName}")
                    println("   - fundType: ${fundDetail.fundType}")
                    println("   - establishDate: ${fundDetail.establishDate}")
                    println("   - fundManager: ${fundDetail.fundManager}")
                    println("   - fundCompany: ${fundDetail.fundCompany}")
                    println("   - investmentObjective 长度: ${fundDetail.investmentObjective.length}")
                    
                    // 5. 获取持仓股数据
                    println("🌐 开始获取持仓股数据...")
                    try {
                        val holdingsHtml = EastMoneyClient.fundDetailApi.getFundHoldings(fundCode = fundCode)
                        println("✅ 持仓股HTML获取成功，长度: ${holdingsHtml.length}")
                        
                        val holdings = FundDetailParser.parseHoldings(holdingsHtml)
                        println("✅ 持仓股解析成功，数量: ${holdings.size}")
                        
                        // 更新fundDetail的持仓股信息
                        if (holdings.isNotEmpty()) {
                            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                            fundDetail = fundDetail.copy(
                                topHoldingsJson = json.encodeToString(
                                    kotlinx.serialization.builtins.ListSerializer(StockHolding.serializer()),
                                    holdings
                                )
                            )
                            println("✅ 已更新持仓股信息到fundDetail")
                        }
                    } catch (e: Exception) {
                        println("⚠️ 获取持仓股失败: ${e.message}")
                        // 持仓股获取失败不影响主流程
                    }
                }
                
                // 6. 保存到本地缓存
                fundDetail?.let {
                    println("💾 保存到本地缓存...")
                    fundDetailCacheDao.insert(it)
                    println("✅ 缓存保存成功")
                }
                
                println("🔍 ========== 获取基金详情结束 ==========")
                Result.success(fundDetail)
            } catch (e: Exception) {
                println("❌ getFundDetailInfo 异常: ${e.message}")
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }
    
    /**
     * 清理过期的基金详情缓存（30天以上）
     */
    suspend fun clearExpiredFundDetails() {
        withContext(Dispatchers.IO) {
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            fundDetailCacheDao.deleteExpiredCache(thirtyDaysAgo)
        }
    }
    
    /**
     * 获取基金净值历史
     * @param fundCode 基金代码
     * @param pageSize 获取数量（7=最近7天, 60=最近60天）
     * @param forceRefresh 是否强制刷新
     */
    suspend fun getFundNavHistory(
        fundCode: String,
        pageSize: Int = 20,
        forceRefresh: Boolean = false
    ): Result<List<FundNav>> = withContext(Dispatchers.IO) {
        try {
            // 1. 检查缓存（24小时内有效）
            if (!forceRefresh) {
                val cache = fundNavCacheDao.getNavCache(fundCode)
                if (cache != null) {
                    val cacheAge = System.currentTimeMillis() - cache.lastUpdateTime
                    val oneDayInMillis = 24 * 60 * 60 * 1000L
                    
                    if (cacheAge < oneDayInMillis) {
                        println("📊 [Nav] 使用缓存数据: $fundCode (${cache.navList.size}条)")
                        val navList = cache.navList.take(pageSize).map {
                            FundNav(
                                date = it.date,
                                nav = it.nav,
                                accumulatedNav = it.accumulatedNav,
                                changeRate = it.changeRate
                            )
                        }
                        return@withContext Result.success(navList)
                    }
                }
            }
            
            // 2. 从API获取
            println("📊 [Nav] 从API获取净值历史: $fundCode, pageSize=$pageSize")
            val response = EastMoneyClient.fundNavApi.getFundNavHistory(
                fundCode = fundCode,
                pageIndex = 1,
                pageSize = 60  // 总是获取60条用于缓存
            )
            
            // 详细日志
            println("📊 [Nav] API响应详情:")
            println("  - ErrCode: ${response.ErrCode}")
            println("  - ErrMsg: ${response.ErrMsg}")
            println("  - TotalCount: ${response.TotalCount}")
            println("  - Data: ${response.Data}")
            println("  - Data.LSJZList: ${response.Data?.LSJZList}")
            println("  - LSJZList size: ${response.Data?.LSJZList?.size}")
            
            // 检查API响应
            if (!response.isSuccess()) {
                println("❌ [Nav] API返回错误: ErrCode=${response.ErrCode}, ErrMsg=${response.ErrMsg}, Data=${response.Data}")
                return@withContext Result.failure(Exception("API返回错误: ${response.ErrMsg}"))
            }
            
            if (response.Data?.LSJZList != null) {
                val navList = response.Data.LSJZList.mapNotNull { item ->
                    try {
                        FundNav(
                            date = item.FSRQ,
                            nav = item.DWJZ.toDoubleOrNull() ?: 0.0,
                            accumulatedNav = item.LJJZ.toDoubleOrNull() ?: 0.0,
                            changeRate = item.JZZZL.toDoubleOrNull() ?: 0.0
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                
                // 3. 缓存数据
                if (navList.isNotEmpty()) {
                    val cacheEntity = FundNavCacheEntity(
                        fundCode = fundCode,
                        navList = navList.map {
                            FundNavItemCache(
                                date = it.date,
                                nav = it.nav,
                                accumulatedNav = it.accumulatedNav,
                                changeRate = it.changeRate
                            )
                        },
                        lastUpdateTime = System.currentTimeMillis()
                    )
                    fundNavCacheDao.insertNavCache(cacheEntity)
                    println("📊 [Nav] 已缓存净值数据: ${navList.size}条")
                }
                
                // 4. 返回请求的数量
                Result.success(navList.take(pageSize))
            } else {
                println("❌ [Nav] API返回空数据")
                Result.failure(Exception("无法获取净值数据"))
            }
        } catch (e: Exception) {
            println("❌ [Nav] 获取净值历史失败: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * 清理过期的净值缓存
     */
    suspend fun clearExpiredNavCache() = withContext(Dispatchers.IO) {
        try {
            val twoDaysAgo = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L)
            fundNavCacheDao.deleteExpiredCache(twoDaysAgo)
            println("🗑️ [Nav] 已清理过期缓存")
        } catch (e: Exception) {
            println("❌ [Nav] 清理缓存失败: ${e.message}")
        }
    }
    
    /**
     * 根据基金代码获取基金名称（用于持仓页面）
     */
    suspend fun getFundNameByCode(fundCode: String): Result<String?> = withContext(Dispatchers.IO) {
        try {
            // 1. 先从缓存查找
            val cached = fundCacheDao.getFundByCode(fundCode)
            if (cached != null) {
                println("📋 从缓存获取基金名称: ${cached.name}")
                return@withContext Result.success(cached.name)
            }
            
            // 2. 从基金索引查找（包含全部25234只基金）
            val allFunds = FundIndexData.loadFunds(context)
            val indexFund = allFunds.find { it.code == fundCode }
            if (indexFund != null) {
                println("📋 从索引获取基金名称: ${indexFund.name}")
                return@withContext Result.success(indexFund.name)
            }
            
            // 3. 尝试从实时API获取
            println("📋 从API获取基金信息: $fundCode")
            val fund = RealDataFetcher.fetchFundRealTime(fundCode)
            if (fund != null) {
                // 缓存数据
                fundCacheDao.insert(fund.toEntity())
                println("📋 从API获取基金名称: ${fund.name}")
                return@withContext Result.success(fund.name)
            }
            
            println("⚠️ 无法获取基金名称")
            Result.success(null)
        } catch (e: Exception) {
            println("❌ 获取基金名称失败: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * 根据日期获取基金净值
     * 如果指定日期是非交易日，则自动查找该日期之后的第一个交易日的净值
     * @param fundCode 基金代码
     * @param date 日期 (yyyy-MM-dd)
     * @return Pair(净值, 实际交易日期)，如果无法获取返回null
     */
    suspend fun getFundNavByDate(fundCode: String, date: String): Result<Pair<Double, String>?> = withContext(Dispatchers.IO) {
        try {
            println("📊 [Nav] 查询特定日期净值: $fundCode, date=$date")
            
            // 先尝试从缓存中查找
            val cached = fundNavCacheDao.getNavCache(fundCode)
            if (cached != null) {
                // 首先尝试精确匹配
                val exactMatch = cached.navList.find { it.date == date }
                if (exactMatch != null) {
                    println("📊 [Nav] 从缓存找到精确日期净值: ${exactMatch.nav}")
                    return@withContext Result.success(Pair(exactMatch.nav, exactMatch.date))
                }
                
                // 如果没有精确匹配，查找该日期之后的第一个交易日
                val nextTradingDay = cached.navList
                    .filter { it.date >= date }
                    .minByOrNull { it.date }
                if (nextTradingDay != null) {
                    println("📊 [Nav] 从缓存找到下一个交易日净值: ${nextTradingDay.date}, nav=${nextTradingDay.nav}")
                    return@withContext Result.success(Pair(nextTradingDay.nav, nextTradingDay.date))
                }
            }
            
            // 从API获取更多历史数据来查找
            println("📊 [Nav] 从API查询历史净值...")
            val response = EastMoneyClient.fundNavApi.getFundNavHistory(
                fundCode = fundCode,
                pageIndex = 1,
                pageSize = 60  // 获取更多数据以覆盖更多日期
            )
            
            if (!response.isSuccess() || response.Data?.LSJZList == null) {
                println("❌ [Nav] API查询失败")
                return@withContext Result.failure(Exception("无法获取净值历史"))
            }
            
            val navList = response.Data.LSJZList
            
            // 先尝试精确匹配
            val exactMatch = navList.find { it.FSRQ == date }
            if (exactMatch != null) {
                val nav = exactMatch.DWJZ.toDoubleOrNull() ?: 0.0
                println("📊 [Nav] 找到精确日期净值: $nav")
                return@withContext Result.success(Pair(nav, date))
            }
            
            // 如果没有精确匹配，查找该日期之后的第一个交易日
            val nextTradingDay = navList
                .filter { it.FSRQ >= date }
                .minByOrNull { it.FSRQ }
            
            if (nextTradingDay != null) {
                val nav = nextTradingDay.DWJZ.toDoubleOrNull() ?: 0.0
                println("📊 [Nav] $date 是非交易日，使用下一个交易日 ${nextTradingDay.FSRQ} 的净值: $nav")
                Result.success(Pair(nav, nextTradingDay.FSRQ))
            } else {
                println("⚠️ [Nav] 未找到日期 $date 及之后的净值")
                Result.failure(Exception("未找到该日期及之后的净值"))
            }
        } catch (e: Exception) {
            println("❌ [Nav] 查询日期净值失败: ${e.message}")
            Result.failure(e)
        }
    }
}

// 扩展函数：Fund转Entity
private fun Fund.toEntity(): FundCacheEntity {
    return FundCacheEntity(
        code = code,
        name = name,
        type = type,
        nav = nav,
        accumulatedNav = accumulatedNav,
        changeRate = changeRate,
        changeAmount = changeAmount,
        manager = manager,
        company = company,
        scale = scale,
        establishDate = establishDate
    )
}

// 扩展函数：Entity转Fund
private fun FundCacheEntity.toFund(): Fund {
    return Fund(
        code = code,
        name = name,
        type = type,
        nav = nav,
        accumulatedNav = accumulatedNav,
        changeRate = changeRate,
        changeAmount = changeAmount,
        manager = manager,
        company = company,
        scale = scale,
        establishDate = establishDate
    )
}

