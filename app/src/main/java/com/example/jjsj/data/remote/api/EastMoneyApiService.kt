package com.example.jjsj.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 天天基金（东方财富）API服务
 */
interface EastMoneyApiService {
    
    /**
     * 获取基金列表
     * http://fund.eastmoney.com/js/fundcode_search.js
     */
    @GET("js/fundcode_search.js")
    suspend fun getFundList(): String
    
    /**
     * 获取基金净值数据
     * http://fundgz.1234567.com.cn/js/{fundCode}.js
     */
    @GET("js/{fundCode}.js")
    suspend fun getFundNav(
        @retrofit2.http.Path("fundCode") fundCode: String
    ): String
    
    /**
     * 获取基金详情
     * http://fund.eastmoney.com/{fundCode}.html
     */
    @GET("{fundCode}.html")
    suspend fun getFundDetail(
        @retrofit2.http.Path("fundCode") fundCode: String
    ): String
}

/**
 * 天天基金实时数据API
 * 基金估值实时接口
 */
interface FundGzApiService {
    /**
     * 获取基金实时估值
     * http://fundgz.1234567.com.cn/js/{fundCode}.js
     * 返回JSONP格式: jsonpgz({"fundcode":"000001","name":"华夏成长","jzrq":"2023-10-13","dwjz":"1.2345",...});
     */
    @GET("js/{fundCode}.js")
    suspend fun getFundRealTime(
        @retrofit2.http.Path("fundCode") fundCode: String
    ): String
}

/**
 * 新浪财经API - 用于获取指数数据
 */
interface SinaFinanceApiService {
    /**
     * 获取指数实时数据（新浪财经）
     * http://hq.sinajs.cn/list=s_sh000001,s_sz399001
     * 返回格式: var hq_str_s_sh000001="上证指数,3088.8039,23.6001,0.77,1041143,13323944";
     */
    @GET("list")
    suspend fun getIndexData(
        @Query(value = "list", encoded = false) codes: String  // 不编码，保留逗号
    ): String
    
    /**
     * 获取单个指数数据（东方财富接口）
     * http://push2.eastmoney.com/api/qt/stock/get?secid=1.000001&fields=f58,f43,f169,f170,f46,f60
     * secid格式：市场代码.指数代码（1=上海，0=深圳）
     * fields: f58=名称,f43=最新价,f169=涨跌额,f170=涨跌幅,f46=成交量,f60=成交额
     */
    @GET("api/qt/stock/get")
    suspend fun getEastMoneyIndexData(
        @Query("secid") secid: String,
        @Query("fields") fields: String = "f58,f43,f169,f170,f46,f60"
    ): String
}

