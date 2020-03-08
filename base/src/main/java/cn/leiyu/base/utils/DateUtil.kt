package cn.leiyu.base.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * 日期处理工具
 */
object DateUtil {
    /**
     * 获取当前的日期 格式为yyyy-MM-dd
     */
    @JvmStatic
    fun getCurrentDate(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    /**
     * 获取当前的日期 格式为yyyy-MM-dd HH:mm:ss
     */
    @JvmStatic
    fun getCurrentDateTime(): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    /**
     * 获取当前的时间 格式为HH:mm:ss
     */
    @JvmStatic
    fun getCurrentTime(): String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
}