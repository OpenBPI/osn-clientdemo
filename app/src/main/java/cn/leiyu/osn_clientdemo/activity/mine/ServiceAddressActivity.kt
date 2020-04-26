package cn.leiyu.osn_clientdemo.activity.mine

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import butterknife.BindView
import cn.leiyu.base.utils.ToastUtil
import cn.leiyu.base.utils.UrlUtil
import cn.leiyu.osn_clientdemo.Constant
import cn.leiyu.osn_clientdemo.IMApp
import cn.leiyu.osn_clientdemo.R
import cn.leiyu.osn_clientdemo.activity.SubBaseActivity

/**
 * 服务器地址设置界面
 */
class ServiceAddressActivity : SubBaseActivity() {

    @BindView(R.id.score_address)
    lateinit var score: EditText
//    @BindView(R.id.candy_address)
//    lateinit var candy: EditText
//    @BindView(R.id.log_address)
//    lateinit var log: EditText

    override fun getLayoutId(): Int {
        return R.layout.activity_service_address
    }

    override fun initView() {
        setTopNavBackground()
        topTitle.text = getString(R.string.setConnection)
        topMenu.text = getString(R.string.confirm)
    }

    override fun initData() {
        val host = if(TextUtils.isEmpty(Constant.API.SERVICE_HOST)){
            Constant.API.SERVICE_HOST = getSharedPreferences(Constant.configFileName, Context.MODE_PRIVATE)
                .getString(Constant.SERVICE_API, "")!!
            Constant.API.SERVICE_HOST
        } else Constant.API.SERVICE_HOST
        score.setText(host)
    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.toolbar_menu){
            if(getString(R.string.confirm) == topMenu.text.toString().trim()){
                val host = score.text.toString().trim()
                //保存信息，验证输入的地址是否符合地址规则
//                if(!checkUrl(host, getString(R.string.setUrl))){
//                    return
//                }
                Constant.API.SERVICE_HOST = host
                getSharedPreferences(Constant.configFileName, Context.MODE_PRIVATE)
                        .edit().putString(Constant.SERVICE_API, host).apply()
                showToast(getString(R.string.success_hint, getString(R.string.setUrl)))
                IMApp.resetSocket()
                setResult(Activity.RESULT_OK)
                finish()
            }
        }else super.onClick(v)
    }

    private fun checkUrl(tmp: String, type: String): Boolean{
        var hint = type
        var isValid = true
        if(TextUtils.isEmpty(tmp)){
            isValid = false
            hint = getString(R.string.dont_null, hint)
        }else if(!UrlUtil.isUrl(tmp)){
            isValid = false
            hint = getString(R.string.address_input_hint, hint)
        }
        if(!isValid) ToastUtil.showCustomToast(context = this, msg= hint)
        return isValid
    }

}