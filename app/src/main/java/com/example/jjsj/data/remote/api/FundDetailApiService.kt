package com.example.jjsj.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 基金详情API服务
 * 从东方财富网站获取基金详细信息HTML
 */
interface FundDetailApiService {
    /**
     * 获取基金基本概况页面
     * @param fundCode 基金代码
     * @return HTML内容
     */
    @GET("jbgk_{fundCode}.html")
    suspend fun getFundBasicInfo(@Path("fundCode") fundCode: String): String
    
    /**
     * 获取基金持仓股数据
     * @param fundCode 基金代码
     * @param topline 前N条数据（默认10）
     */
    @GET("FundArchivesDatas.aspx")
    suspend fun getFundHoldings(
        @Query("type") type: String = "jjcc",
        @Query("code") fundCode: String,
        @Query("topline") topline: Int = 10
    ): String
}

