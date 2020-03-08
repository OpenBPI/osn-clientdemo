package cn.leiyu.osn_clientdemo.utils

import android.graphics.Color
import cn.leiyu.base.App
import cn.leiyu.osn_clientdemo.R
import java.text.SimpleDateFormat
import java.util.*

object ProductLableUtil {

    @JvmField
    val timeFormat = arrayOf("yyyy年MM月dd日 HH:mm", "MM月dd日 HH:mm", "HH:mm")
    /**
     * 随机生产标签颜色 默认三种颜色 红 绿 主题蓝
     */
    @JvmStatic
    fun getLableColor(colors: Array<Int> = arrayOf(Color.RED, Color.GREEN,
        App.getAppContext().resources.getColor(R.color.bg_1484ED))): Int{
        return colors[Random().nextInt(colors.size)]
    }

    @JvmStatic
    fun showTime(second: Long, currentTime: Date, isDetail: Boolean = true): String{
        val sdf = SimpleDateFormat(timeFormat[0], Locale.getDefault())
        sdf.calendar.timeInMillis = second
        val lastTime = sdf.calendar
        val current = SimpleDateFormat(timeFormat[0], Locale.getDefault()).calendar
        current.time = currentTime
        return if(lastTime.get(Calendar.YEAR) == current.get(Calendar.YEAR)){
            //年份相等
            if(lastTime.get(Calendar.MONTH) == current.get(Calendar.MONTH)
                && lastTime.get(Calendar.DAY_OF_MONTH) == current.get(Calendar.DAY_OF_MONTH)){
                //月份 并且 月中天数相等
                if(lastTime.get(Calendar.HOUR_OF_DAY) == current.get(Calendar.HOUR_OF_DAY)
                    && (lastTime.get(Calendar.MINUTE) in (current.get(Calendar.MINUTE).. (current.get(Calendar.MINUTE)+3)))){
                    //省略
                    if(isDetail) "刚刚" else ""
                }else{
                    //显示 时分
                    formatTime(sdf, timeFormat[2], Date(second))
                }
            }else{
                //从月开始 显示到分
                formatTime(sdf, timeFormat[1], Date(second))
            }
        }else{
            //显示到分
            formatTime(sdf, timeFormat[0], Date(second))
        }
    }

    private fun formatTime(sdf: SimpleDateFormat, format: String, time: Date): String{
        sdf.applyPattern(format)
        return sdf.format(time)
    }
}