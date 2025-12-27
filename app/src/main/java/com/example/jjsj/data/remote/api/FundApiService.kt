package com.example.jjsj.data.remote.api

import com.example.jjsj.data.remote.dto.FundDetailResponse
import com.example.jjsj.data.remote.dto.FundListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 基金API服务接口
 */
interface FundApiService {
    /**
     * 获取基金列表
     */
    @GET("api/funds")
    suspend fun getFundList(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("type") type: String? = null
    ): FundListResponse
    
    /**
     * 搜索基金
     */
    @GET("api/funds/search")
    suspend fun searchFunds(
        @Query("keyword") keyword: String
    ): FundListResponse
    
    /**
     * 获取基金详情
     */
    @GET("api/funds/{code}")
    suspend fun getFundDetail(
        @Path("code") code: String
    ): FundDetailResponse
    
    /**
     * 获取自选基金列表
     */
    @GET("api/funds/favorites")
    suspend fun getFavoriteFunds(
        @Query("codes") codes: String
    ): FundListResponse
}

