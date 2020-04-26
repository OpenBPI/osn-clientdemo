package cn.leiyu.base.activity

import android.app.Dialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import cn.leiyu.base.activity.fragment.BaseFragment
import cn.leiyu.base.utils.ToastUtil
import android.app.Activity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import cn.leiyu.base.R
import java.lang.ref.WeakReference


/**
 * 所有自定义Activity的基类
 */
abstract class BaseActivity : BaseActivityApi23(), View.OnClickListener{
    protected var mContext: Context? = null
    var baseFragment: BaseFragment? = null
    companion object {
        var mCurrentActivity: WeakReference<BaseActivity>? = null
        open fun updateNotify(){
            mCurrentActivity?.get().let {
                it?.runOnUiThread(Runnable {
                    it.updateView()
                })
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
            window.statusBarColor = Color.WHITE
        }
        TAG = this::class.simpleName
        mContext = this
        loadViewBefore()
        if(getLayoutId() > 0)
            setContentView(getLayoutId())
        else{
            setContentView(R.layout.base_top_toolbar)
        }
        initView()
        initData()
    }
    override fun onResume() {
        super.onResume()
        mCurrentActivity = WeakReference(this)
    }
    fun showToast(msg: String) {
        ToastUtil.showCustomToast(context = this, msg = msg)
    }
    /**
     * 点击事件回调
     */
    override fun onClick(v: View?) {
        baseFragment?.onClick(v)
    }

    /**
     * 加载布局文件之前
     */
    protected open fun loadViewBefore(){}
    /**
     * 设置当前布局文件资源
     */
    protected abstract fun getLayoutId(): Int

    /**
     * 初始化view
     */
    protected abstract fun initView()

    /**
     * 初始化数据
     */
    protected abstract fun initData()
    open fun updateView(){}

    fun MIUISetStatusBarLightMode(window: Window, darkmode: Boolean): Boolean {
        val clazz = window::javaClass
        try {
//            var darkModeFlag = 0
//            val layoutParams = "android.view.MiuiWindowManager$LayoutParams"
//            val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
//            darkModeFlag = field.getInt(layoutParams)
//            val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
//            extraFlagField.invoke(window, if (darkmode) darkModeFlag else 0, darkModeFlag)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun FlymeSetStatusBarLightMode(window: Window?, dark: Boolean): Boolean {
        var result = false
        if (window != null) {
            try {
                val lp = window.attributes
                val darkFlag = WindowManager.LayoutParams::class.java!!
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                val meizuFlags = WindowManager.LayoutParams::class.java!!
                        .getDeclaredField("meizuFlags")
                darkFlag.setAccessible(true)
                meizuFlags.setAccessible(true)
                val bit = darkFlag.getInt(null)
                var value = meizuFlags.getInt(lp)
                if (dark) {
                    value = value or bit
                } else {
                    value = value and bit.inv()
                }
                meizuFlags.setInt(lp, value)
                window.attributes = lp
                result = true
            } catch (e: Exception) {

            }

        }
        return result
    }

    /**
     * 显示自定义对话框
     */
    open fun showDialog(title: String, contentId: Int = 0, isDoubleBtn: Boolean = true, type: Int = 0): Dialog{
        val dialog = Dialog(this, R.style.dialog)
        val contentView = layoutInflater.inflate(R.layout.base_alert, null, false)
        val titleText : TextView = contentView.findViewById(R.id.alertTitle)
        titleText.text = title
        if(contentId > 0){
            val contentFrame :FrameLayout = contentView.findViewById(R.id.alertContent)
            contentFrame.visibility = View.VISIBLE
            val tmpView = LayoutInflater.from(this).inflate(contentId, contentFrame, true)
            onAlertView(tmpView, type)
        }
        val llBtn: LinearLayout = contentView.findViewById(R.id.double_btn)
        val sigleBtn: TextView = contentView.findViewById(R.id.sigle_sure)
        if(!isDoubleBtn){
            llBtn.visibility = View.INVISIBLE
            sigleBtn.visibility = View.VISIBLE
            setAlertClick(sigleBtn, dialog, type)
        }else{
            llBtn.visibility = View.VISIBLE
            sigleBtn.visibility = View.INVISIBLE
            setAlertClick(contentView.findViewById(R.id.double_sure), dialog, type)
            setAlertClick(contentView.findViewById(R.id.double_cancel), dialog, type)
        }
        dialog.setContentView(contentView)
        dialog.show()
        val lp = dialog.window?.attributes
        val width =resources.displayMetrics.widthPixels - resources.getDimensionPixelOffset(R.dimen.dp_80)
        lp!!.width = width
        dialog.window!!.attributes = lp
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    /**
     * 自定义对话框的内容布局回调
     */
    open fun onAlertView(v: View, type: Int){
        baseFragment?.onAlertView(v, type)
    }

    private fun setAlertClick(v: View, dialog: Dialog, type: Int){
        v.setOnClickListener{
            dialog.dismiss()
            onClick(it)
        }
        v.contentDescription = "$type"
    }
}