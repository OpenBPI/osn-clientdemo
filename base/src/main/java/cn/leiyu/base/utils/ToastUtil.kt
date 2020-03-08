package cn.leiyu.base.utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import cn.leiyu.base.R
import java.util.*

object ToastUtil {
    private var sToast:Toast? = null
    private var isShow:Boolean = false

    /**
     * 显示自定义toast
     */
    fun showCustomToast(context: Context, resId: Int = R.layout.toast_custom_common, msg: String){
        var id = resId
        if(id <= 0)id = R.layout.toast_custom_common
        if(sToast == null){
            sToast = Toast(context)
            sToast!!.view = LayoutInflater.from(context).inflate(id, null, false)
        }else {
            if(id != R.layout.toast_custom_common){
                sToast!!.view = LayoutInflater.from(context).inflate(id, null, false)
            }
            if(!isShow)update()
            else return
        }
        with(sToast){
            this?.setText(msg)
            this?.duration = Toast.LENGTH_LONG
            this?.show()
        }
    }

    private fun update(){
        Timer().schedule(object :TimerTask(){
            override fun run() {
                isShow = false
                sToast = null
            }
        }, 2500)
        isShow = true
    }
}