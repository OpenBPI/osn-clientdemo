package cn.leiyu.base.utils

import android.content.Context
import java.lang.ref.WeakReference

/**
 * 拦截虚拟机的崩溃处理
 */
class CrashHandler private constructor(): Thread.UncaughtExceptionHandler {

    private var context :WeakReference<Context>? = null
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    /**
     * 初始化拦截
     */
    fun init(context: Context){
        this.context = WeakReference(context)
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread?, ex: Throwable?) {
        if(ex != null){
            PrintWriteUtils.writeErrorLog(true, getCrashReport(ex))
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(0)
        }else defaultHandler?.uncaughtException(thread, ex)
    }

    /**
     * 获取APP崩溃异常报告
     * @param ex
     * @return
     */
    private fun getCrashReport(ex: Throwable): String {
        val exceptionStr = StringBuffer()
        context?.get()?.let {
            val packageInfo = it.packageManager.getPackageInfo(it.packageName, 0)
            exceptionStr.append("Version: ${packageInfo.versionName} (${packageInfo.versionCode})\n")
            exceptionStr.append("Android: ${android.os.Build.VERSION.RELEASE} (${android.os.Build.MODEL})\n")
            exceptionStr.append("Exception: " + ex.message + "\n")
            val elements = ex.stackTrace
            for (i in elements.indices) {
                exceptionStr.append(elements[i].toString() + "\n")
            }
        }
        return exceptionStr.toString()
    }

    companion object {
        private val lock = Any()
        @JvmStatic
        private var unHandler:CrashHandler? = null

        fun getInstance(): CrashHandler{
            synchronized(lock){
                return unHandler ?: CrashHandler()
            }
        }
    }
}