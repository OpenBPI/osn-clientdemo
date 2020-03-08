package cn.leiyu.osn_clientdemo.utils

import java.util.regex.Pattern

/**
 * 正则工具
 */
object PatternUtil {
    /**
     * 是否是中文
     * @param str
     * @return
     */
    @JvmStatic
    fun isChinese(str: String): Boolean{
        val p = Pattern.compile("[\\u4e00-\\u9fa5]")
        return p.matcher(str).find()
    }

    /**
     * 是否是双字节字符信息
     * @param str
     * @return
     */
     fun isDoubleChar(str: String): Boolean{
        val p = Pattern.compile("[^\\x00-\\xff]")
        return p.matcher(str).find()
    }

    /**
     * 是否是正确的电子邮箱
     */
    fun isMail(mail: String): Boolean{
        val p = Pattern.compile("^([a-zA-Z0-9_\\.\\-])+\\@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$")
        return p.matcher(mail).find()
    }
}