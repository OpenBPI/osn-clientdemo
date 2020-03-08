@file:JvmMultifileClass
package cn.leiyu.base

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import cn.leiyu.base.db.AbsDBManager
import cn.leiyu.base.utils.CrashHandler
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

/**
 * 应用程序上下文
 */
lateinit var mAppContext: Context

/**
 * 应用程序上下文
 * 加入分包方式<br/>
 * @create at 2018-06-11 by Yao
 */
open class App : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        CrashHandler.getInstance().init(this)
        mAppContext = this
        queue = Volley.newRequestQueue(this)
    }

    /**
     * 获取数据库管理对象
     * @param type 类型
     */
    open fun getDBManager(type: Int): AbsDBManager?{ return null}

    companion object Tmp{
        /**
         * 应用程序上下文,能全局调用
         */
        @JvmStatic
        fun getAppContext(): Context {
            return mAppContext
        }

        @JvmStatic
        lateinit var queue: RequestQueue
    }
}