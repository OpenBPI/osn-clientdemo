package cn.leiyu.base.utils

import android.util.Log
import cn.leiyu.base.BuildConfig

/**
 * 日志打印工具
 */
object LogUtil{
    @JvmStatic
    fun e(tag :String = "LogUtil", msg :String?){
        e(tag, msg, false)
    }

    @JvmStatic
    fun d(tag :String = "LogUtil", msg :String?){
        if(BuildConfig.DEBUG) Log.d(tag, "--------ddd------->$msg")
        else PrintWriteUtils.writeErrorLog(str ="$tag----------$msg")
    }

    @JvmStatic
    fun i(tag :String = "LogUtil", msg :String?){
        if(BuildConfig.DEBUG)Log.i(tag, "--------iii------->$msg")
        else PrintWriteUtils.writeErrorLog(str ="$tag----------$msg")
    }

    /**
     * @param onlyLog true 只打印信息，不记录信息
     */
    @JvmStatic
    fun e(tag :String = "LogUtil", msg :String?, onlyLog :Boolean = true){
        if(BuildConfig.DEBUG || onlyLog)Log.e(tag, "--------eee------->$msg")
        else PrintWriteUtils.writeErrorLog(str ="$tag----------$msg")
    }
}