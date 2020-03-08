package cn.leiyu.base.utils

import android.text.TextUtils
import cn.leiyu.base.App
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset

/**
 * 日志记录工具<br/>
 * 文件存放位置：优先存放到sdcard的cache/files/log目录，如果没有就存放到项目私有cache/files/log里; 否则可能存放到根目录（最坏的情况）
 */
object PrintWriteUtils {

    /**
     * 创建日志文件
     * @param isCrash 是否是崩溃日志，如果为true文件名会带上crash，加以区分；否则就是平常的日志记录
     * @return 返回文件对象
     */
    private fun createLogFile(isCrash: Boolean = false): File{
        var path = App.getAppContext().externalCacheDir!!.parentFile!!.absolutePath
        if(TextUtils.isEmpty(path)){
            path = App.getAppContext().cacheDir!!.parentFile!!.absolutePath
        }
        path?.let {
            val sdf = DateUtil.getCurrentDate()
            path = "$path/files/log/$sdf${if(isCrash)"crash.txt" else ".txt"}"
        }
        val file = File(path)
        if(!file.parentFile.exists())file.parentFile.mkdirs()
        return file
    }

    /**
     * 将信息记录到当日的文件中
     * @param isCrash 是否为崩溃日志，默认否
     * @param str 信息
     */
    @JvmStatic
    fun writeErrorLog(isCrash: Boolean = false, str :String){
        val outFile = FileOutputStream(createLogFile(isCrash), true)
        outFile.write("${DateUtil.getCurrentTime()}:\t$str\r\n".toByteArray(Charset.forName("GBK")))
        outFile.flush()
        outFile.close()
    }
}