package cn.leiyu.base.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import android.widget.Toast
import cn.leiyu.base.R
import java.util.ArrayList
import java.util.HashMap

/**
 * 系统Api23 兼容工具类
 * @author Yao
 */
class Api23Util(private val context :Activity, private val callback :IPermissionGrantCallback){

    var mPermissionText: MutableMap<String, String> = HashMap(3)
    var mPermissionCode: Int = 201
    /**
     * @return 获取权限文本信息，方便界面自动配置，默认添加电话状态，相机，存储卡权限文本，见{@link Api23Util#initPermissionText()}
     */
    fun getPermissionText()= mPermissionText

    /**
     * 是否授予运行时权限sdk >= 23
     * @param requestCode 请求码，用于权限结果验证
     * @param permissions 所要申请的权限
     * @return 是否要授权
     */
    fun isGrant(requestCode: Int, permission :Array<String>){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            mPermissionCode = requestCode
            val tmpPermission = ArrayList<String>(1)
            for(tmp in permission){
                val grant = context.checkSelfPermission(tmp)
                if(grant != PackageManager.PERMISSION_GRANTED){
                    tmpPermission.add(tmp)
                }
            }
            if(!tmpPermission.isEmpty()){
                initPermissionText()
                context.requestPermissions(tmpPermission.toTypedArray(), mPermissionCode)
                return
            }
        }
        callback.permissionGrant(mPermissionCode, permission)
    }

    /**
     * 权限拒绝结果处理
     * @param requestCode 请求码
     * @param tmp 拒绝权限
     */
    fun permissionDenied(requestCode: Int, tmp: Array<String>){
        val sb = StringBuffer("")
        for (str in tmp) {
            if (mPermissionText.containsKey(str)) {
                val txt = mPermissionText[str]
                if (sb.indexOf(txt!!) == -1) sb.append("$txt、")
            }
        }
        var str = ""
        if(!TextUtils.isEmpty(sb.toString())){
            str = sb.substring(0, sb.length - 1)
        }
        if(callback.isCustomHint()){
            callback.permissionDenied(mPermissionCode, str)
        }else{
            str = context.getString(R.string.permission_hint, context.getString(R.string.app_name), str)
            Toast.makeText(context.applicationContext, str, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 权限结果（Activity回调方法）
     * @param requestCode 请求码
     * @param permissions 请求权限
     * @param grantResults 授权结果
     */
    fun onRequestPermissionsResult(requestCode: Int, permissions :Array<out String>, grantResults :IntArray){
        if (requestCode == mPermissionCode) {
            val tmp = ArrayList<String>(1)
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    tmp.add(permissions[i])
                }
            }
            if (tmp.size > 0) {
                permissionDenied(mPermissionCode, tmp.toTypedArray())
            } else {
                callback?.permissionGrant(mPermissionCode, permissions)
            }
        }
    }

    //设置权限文本信息
    private fun initPermissionText(){
        mPermissionText[Manifest.permission.READ_PHONE_STATE] = "电话"
        mPermissionText[Manifest.permission.CAMERA] = "相机"
        mPermissionText[Manifest.permission.READ_EXTERNAL_STORAGE] = "存储空间"
        mPermissionText[Manifest.permission.WRITE_EXTERNAL_STORAGE] = "存储空间"
    }

    /**
     * SDK>23 运行时权限回调接口
     */
    interface IPermissionGrantCallback{
        /**
         * 是否授予必要权限
         * @param requestCode 请求码
         * @param permissions 请求权限
         */
        fun isGrant(requestCode :Int, permission: Array<String>)

        /**
         * 权限拒绝处理
         * @param requestCode 请求码
         * @param tmp 拒绝的权限文本
         */
        fun permissionDenied(requestCode :Int, tmp :String)

        /**
         * 权限授予的处理
         * @param requestCode 请求码
         * @param permission 授予的权限
         */
        fun permissionGrant(requestCode :Int, permission: Array<out String>)

        fun isCustomHint(): Boolean

        fun getPermissionText(): Map<String, String>
    }
}