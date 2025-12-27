package com.example.jjsj.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 日期工具类
 */
object DateUtils {
    private val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT, Locale.CHINA)
    private val dateTimeFormat = SimpleDateFormat(Constants.DATETIME_FORMAT, Locale.CHINA)
    
    /**
     * 获取当前日期字符串
     */
    fun getCurrentDate(): String {
        return dateFormat.format(Date())
    }
    
    /**
     * 获取当前日期时间字符串
     */
    fun getCurrentDateTime(): String {
        return dateTimeFormat.format(Date())
    }
    
    /**
     * 时间戳转日期字符串
     */
    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }
    
    /**
     * 时间戳转日期时间字符串
     */
    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }
    
    /**
     * 日期字符串转时间戳
     */
    fun parseDate(dateString: String): Long {
        return try {
            dateFormat.parse(dateString)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}

