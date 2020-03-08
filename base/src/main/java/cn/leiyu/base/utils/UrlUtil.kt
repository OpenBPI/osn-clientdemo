package cn.leiyu.base.utils

import android.net.Uri
import android.text.TextUtils

/**
 * url地址判断工具
 */
object UrlUtil {

    fun isUrl(str: String): Boolean{
        if(str.startsWith("http://") || str.startsWith("https://"))return true
        return false
    }

    fun get3W(str: String): String{
        val uri = Uri.parse(str)
        val port = if(uri.port < 0)"" else ":${uri.port}"
        return "${uri.scheme}://"+uri.host+port
    }

    /**
     * 解析url中的参数信息
     * @url 地址信息
     * @key 是否解析指定参数 默认为空，解析全部参数
     */
    fun getUrlParams(url: String, key: String?= ""): Any{
        val uri = Uri.parse(url)
        return if(TextUtils.isEmpty(key)){
            val names = uri.queryParameterNames
            val map = HashMap<String, String>()
            for (name in names) {
                val value = uri.getQueryParameter(name)
                map[name] = value!!
            }
            map
        }else{
            uri.getQueryParameter(key)!!
        }
    }
}