package com.example.jjsj.data.remote

import com.example.jjsj.data.remote.api.EastMoneyApiService
import com.example.jjsj.data.remote.api.FundDetailApiService
import com.example.jjsj.data.remote.api.FundGzApiService
import com.example.jjsj.data.remote.api.FundNavApiService
import com.example.jjsj.data.remote.api.SinaFinanceApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/**
 * 天天基金API客户端
 */
object EastMoneyClient {
    
    /**
     * 自定义 DNS 解析器
     * 解决 Android 模拟器 DNS 解析问题
     */
    private object CustomDns : Dns {
        // 硬编码域名的 IP 地址（通过 nslookup 查询得到）
        private val domainIpMap = mapOf(
            "fundgz.1234567.com.cn" to listOf(
                "119.96.18.107",
                "119.96.17.168",
                "58.49.196.107"
            ),
            "hq.sinajs.cn" to listOf(
                "125.94.246.104"
            ),
            "push2.eastmoney.com" to listOf(
                "101.226.30.206",  // 东方财富行情推送服务器
                "101.226.61.122",  // 备用IP
                "58.58.27.50"      // 备用IP
            ),
            "fundf10.eastmoney.com" to listOf(
                "121.14.142.144",  // 基金详情页面服务器
                "183.60.155.246",
                "27.21.227.108",
                "119.96.18.107"
            ),
            "api.fund.eastmoney.com" to listOf(
                "61.129.129.60"    // 基金净值历史API服务器
            )
        )
        
        override fun lookup(hostname: String): List<InetAddress> {
            // 如果是我们关心的域名，使用硬编码的 IP
            domainIpMap[hostname]?.let { ips ->
                return try {
                    ips.map { InetAddress.getByName(it) }
                } catch (e: Exception) {
                    // 如果硬编码的 IP 也失败，尝试系统 DNS
                    trySystemDns(hostname)
                }
            }
            
            // 其他域名使用系统 DNS
            return trySystemDns(hostname)
        }
        
        private fun trySystemDns(hostname: String): List<InetAddress> {
            return try {
                Dns.SYSTEM.lookup(hostname)
            } catch (e: UnknownHostException) {
                // 如果系统 DNS 失败，抛出异常
                throw UnknownHostException("Unable to resolve host: $hostname")
            }
        }
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.NONE  // 关闭HTTP日志
    }
    
    /**
     * 添加User-Agent拦截器
     * 根据不同的host设置不同的Referer
     */
    private val userAgentInterceptor = okhttp3.Interceptor { chain ->
        val originalRequest = chain.request()
        val host = originalRequest.url.host
        
        // 根据不同的域名设置不同的Referer
        val referer = when {
            host.contains("api.fund.eastmoney.com") -> "http://fundf10.eastmoney.com/"
            host.contains("fundf10.eastmoney.com") -> "http://fundf10.eastmoney.com/"
            host.contains("sinajs.cn") -> "https://finance.sina.com.cn/"
            else -> "http://fund.eastmoney.com/"
        }
        
        val request = originalRequest.newBuilder()
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
            .header("Referer", referer)
            .build()
        chain.proceed(request)
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .dns(CustomDns) // 使用自定义 DNS 解析器
        .addInterceptor(userAgentInterceptor) // 添加User-Agent
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    
    // 天天基金主站API
    private val eastMoneyRetrofit = Retrofit.Builder()
        .baseUrl("http://fund.eastmoney.com/")
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
    
    // 基金估值实时API
    private val fundGzRetrofit = Retrofit.Builder()
        .baseUrl("http://fundgz.1234567.com.cn/")
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
    
    // 新浪财经API
    private val sinaRetrofit = Retrofit.Builder()
        .baseUrl("http://hq.sinajs.cn/")
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
    
    // 东方财富行情API（用于指数数据）
    private val eastMoneyQuoteRetrofit = Retrofit.Builder()
        .baseUrl("http://push2.eastmoney.com/")
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
    
    // 基金详情页面API
    private val fundDetailRetrofit = Retrofit.Builder()
        .baseUrl("https://fundf10.eastmoney.com/")
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
    
    // 基金净值历史API
    private val fundNavRetrofit = Retrofit.Builder()
        .baseUrl("http://api.fund.eastmoney.com/")
        .client(okHttpClient)
        .addConverterFactory(Json {
            ignoreUnknownKeys = true
            isLenient = true
        }.asConverterFactory("application/json".toMediaType()))
        .build()
    
    val eastMoneyApi: EastMoneyApiService = eastMoneyRetrofit.create(EastMoneyApiService::class.java)
    val fundGzApi: FundGzApiService = fundGzRetrofit.create(FundGzApiService::class.java)
    val sinaFinanceApi: SinaFinanceApiService = sinaRetrofit.create(SinaFinanceApiService::class.java)
    val eastMoneyQuoteApi: SinaFinanceApiService = eastMoneyQuoteRetrofit.create(SinaFinanceApiService::class.java)
    val fundDetailApi: FundDetailApiService = fundDetailRetrofit.create(FundDetailApiService::class.java)
    val fundNavApi: FundNavApiService = fundNavRetrofit.create(FundNavApiService::class.java)
}

