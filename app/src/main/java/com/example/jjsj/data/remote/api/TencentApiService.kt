package com.example.jjsj.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 腾讯证券API服务
 */
interface TencentApiService {
    
    /**
     * 获取股票/指数行情数据
     * @param codes 股票/指数代码，多个用逗号分隔，如 "sz399989,sz399967"
     * @return 返回包含行情数据的字符串
     */
    @GET("http://qt.gtimg.cn/q")
    suspend fun getQuotes(@Query("q") codes: String): String
}

