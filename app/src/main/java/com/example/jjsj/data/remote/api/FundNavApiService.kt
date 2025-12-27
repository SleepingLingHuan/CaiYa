package com.example.jjsj.data.remote.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 基金净值历史数据API服务
 */
interface FundNavApiService {
    /**
     * 获取基金历史净值
     * @param fundCode 基金代码
     * @param pageIndex 页码（从1开始）
     * @param pageSize 每页数量
     */
    @GET("f10/lsjz")
    suspend fun getFundNavHistory(
        @Query("fundCode") fundCode: String,
        @Query("pageIndex") pageIndex: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): FundNavHistoryResponse
}

/**
 * 自定义序列化器：处理Data字段可能是String或Object的情况
 */
object FundNavDataSerializer : KSerializer<FundNavData?> {
    override val descriptor: SerialDescriptor = 
        PrimitiveSerialDescriptor("FundNavData", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: FundNavData?) {
        // 不需要序列化
    }
    
    override fun deserialize(decoder: Decoder): FundNavData? {
        return try {
            val jsonDecoder = decoder as JsonDecoder
            val element = jsonDecoder.decodeJsonElement()
            
            println("🔍 [NavSerializer] 解析Data字段, 类型: ${element::class.simpleName}")
            
            when (element) {
                is JsonObject -> {
                    println("🔍 [NavSerializer] Data是对象: ${element.toString().take(200)}")
                    // 正常的对象格式，忽略未知字段
                    val json = Json { 
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                    val result = json.decodeFromJsonElement(FundNavData.serializer(), element)
                    println("🔍 [NavSerializer] 解析成功，LSJZList size: ${result.LSJZList?.size}")
                    result
                }
                is JsonPrimitive -> {
                    // 字符串格式（错误时）
                    println("🔍 [NavSerializer] Data是字符串: ${element.content}")
                    null
                }
                is JsonNull -> {
                    println("🔍 [NavSerializer] Data是null")
                    null
                }
                else -> {
                    println("🔍 [NavSerializer] Data是其他类型: ${element::class.simpleName}")
                    null
                }
            }
        } catch (e: Exception) {
            println("❌ [NavSerializer] 解析异常: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}

/**
 * 基金净值历史响应
 */
@Serializable
data class FundNavHistoryResponse(
    @Serializable(with = FundNavDataSerializer::class)
    val Data: FundNavData? = null,
    val ErrCode: Int,
    val ErrMsg: String? = null,
    val TotalCount: Int
) {
    // 判断是否成功
    fun isSuccess(): Boolean = ErrCode == 0 && Data != null
}

@Serializable
data class FundNavData(
    val LSJZList: List<FundNavItem>? = null
)

@Serializable
data class FundNavItem(
    val FSRQ: String,           // 净值日期 2025-10-13
    val DWJZ: String,           // 单位净值 1.0970
    val LJJZ: String,           // 累计净值 3.6700
    val JZZZL: String,          // 日增长率 -0.09
    val SGZT: String,           // 申购状态
    val SHZT: String,           // 赎回状态
    val FHFCZ: String? = null,  // 分红送配
    val FHFCBZ: String? = null, // 分红备注
    val SDATE: String? = null,  // 额外字段
    val ACTUALSYI: String? = null,
    val NAVTYPE: String? = null,
    val DTYPE: String? = null,
    val FHSP: String? = null
)

