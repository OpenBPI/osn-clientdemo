@file:JvmName("BaseActivityApi23")
package cn.leiyu.base.activity

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.leiyu.base.R
import cn.leiyu.base.utils.Api23Util

/**
 * 该抽象类为所有activity的基类
 * @author Yao
 */
abstract class BaseActivityApi23 : AppCompatActivity(), Api23Util.IPermissionGrantCallback {

    var TAG: String? = null
    private var mApi23: Api23Util? = null

    /**
     * api 23以上的处理对象
     */
    fun getApi23Util() = mApi23

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TAG = this.javaClass.simpleName
        mApi23 = Api23Util(this, this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mApi23?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun isGrant(requestCode: Int, permission: Array<String>) {
        mApi23?.let {
            it.isGrant(requestCode, permission)
        }
    }

    override fun permissionDenied(requestCode: Int, tmp: String) {
        showAlertDialog("提示", getString(R.string.permission_hint, getString(R.string.app_name), tmp))
    }

    override fun permissionGrant(requestCode: Int, permission: Array<out String>) {}

    /**
     * 是否自定义权限提示方式
     * @return 默认自定义，false就toast显示
     */
    override fun isCustomHint(): Boolean {
        return true
    }

    override fun getPermissionText(): Map<String, String> {
        mApi23?.let {
            return it.getPermissionText()
        }
        return mapOf()
    }

    /**
     * 权限拒绝显示对话框
     * @param title
     * @param content
     */
    private fun showAlertDialog(title:String, content :String){
        val dialog = Dialog(this, R.style.dialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        val contentView = layoutInflater.inflate(R.layout.base_dialog_hint, null)
        // 初始化控件
        val tv_title :TextView= contentView.findViewById(R.id.tv_dg_double_title)
        val tv_content :TextView = contentView.findViewById(R.id.tv_dg_double_content)
        // 设置标题
        if (TextUtils.isEmpty(title) && !TextUtils.isEmpty(content)) {
            tv_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            tv_content.visibility= View.GONE
            tv_title.text = content
        } else {
            tv_title.text = title
        }
        // 设置内容
        if (TextUtils.isEmpty(content)) {
            tv_content.visibility = View.GONE
        } else {
            if (!TextUtils.isEmpty(title)) {
                tv_content.text = content
            }
        }
        // 设置按钮
        val tv_dg_blue_singel :TextView = contentView.findViewById(R.id.tv_dg_blue_singel)
        tv_dg_blue_singel.text = getString(R.string.confirm)
        tv_dg_blue_singel.setOnClickListener{
            dialog.dismiss()
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            System.exit(0)
        }
        dialog.setContentView(contentView)
        dialog.show()
    }
}