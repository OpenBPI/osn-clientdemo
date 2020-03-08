package cn.leiyu.osn_clientdemo.activity

import android.content.Context
import cn.leiyu.base.activity.BaseActivity
import cn.leiyu.osn_clientdemo.Constant
import cn.leiyu.osn_clientdemo.beans.UserBean
import com.google.gson.Gson

/**
 * 统一处理客户端必要参数
 */
abstract class AbsParentBaseActivity : BaseActivity() {

    /**
     * 获取服务器地址
     */
    fun getServer(index: Int): String{
        val serverApi = getSharedPreferences(Constant.configFileName, Context.MODE_PRIVATE)
                .getStringSet(Constant.SERVICE_API, setOf("0,http://40.73.116.190:8080","1,http://40.73.96.49:8082","2,http://42.159.95.0:8080"))
        for (str in serverApi!!.toTypedArray()){
            val tmp = str.split(",")
            if(index == tmp[0].toInt()){
                return tmp[1]
            }
        }
        return ""
    }

    open fun getUser(): UserBean {
        val info = mContext?.getSharedPreferences(Constant.configFileName, Context.MODE_PRIVATE)
                ?.getString(Constant.CURRENT_ACCOUNT, "{}")
        return Gson().fromJson(info, UserBean::class.java)
    }
}