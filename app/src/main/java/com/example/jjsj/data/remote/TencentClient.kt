package com.example.jjsj.data.remote

import com.example.jjsj.data.remote.api.TencentApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 腾讯证券API客户端
 */
object TencentClient {
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Referer", "http://qt.gtimg.cn/")
                .build()
            chain.proceed(request)
        }
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://qt.gtimg.cn/")
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
    
    val api: TencentApiService = retrofit.create(TencentApiService::class.java)
}

