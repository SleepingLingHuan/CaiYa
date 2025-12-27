package com.example.jjsj.util

/**
 * 应用常量
 */
object Constants {
    // API相关
    const val BASE_URL = "https://api.fund.eastmoney.com/"
    const val CACHE_TIMEOUT = 5 * 60 * 1000L // 5分钟缓存
    
    // 数据库相关
    const val DATABASE_NAME = "fund_tracker_db"
    const val DATABASE_VERSION = 6  // v6: 为PositionEntity添加navConfirmed字段
    
    // 基金类型
    object FundType {
        const val STOCK = "股票型"
        const val BOND = "债券型"
        const val MIXED = "混合型"
        const val INDEX = "指数型"
        const val MONEY = "货币型"
        const val QDII = "QDII"
    }
    
    // 估值范围
    object ValuationRange {
        const val LOW_MIN = 0
        const val LOW_MAX = 30
        const val NORMAL_MIN = 30
        const val NORMAL_MAX = 70
        const val HIGH_MIN = 70
        const val HIGH_MAX = 100
    }
    
    // 日期格式
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
}

