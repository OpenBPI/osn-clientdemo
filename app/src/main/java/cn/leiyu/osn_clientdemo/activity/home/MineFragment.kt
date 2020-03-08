package cn.leiyu.osn_clientdemo.activity.home

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.OnClick
import cn.leiyu.base.activity.BaseActivity
import cn.leiyu.osn_clientdemo.Constant
import cn.leiyu.osn_clientdemo.R
import cn.leiyu.osn_clientdemo.activity.AbsParentBaseActivity
import cn.leiyu.osn_clientdemo.activity.SubBaseFragment
import cn.leiyu.osn_clientdemo.activity.mine.ServiceAddressActivity
import cn.leiyu.osn_clientdemo.beans.UserBean
import cn.leiyu.osn_clientdemo.db.LocalDBManager
import cn.leiyu.osn_clientdemo.db.tables.UserOperaDao
import com.google.gson.Gson
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.utils.BitMatrix2BitmapUtil

/**
 * 主界面 - 我布局
 */
class MineFragment : SubBaseFragment() {

    val FLAG_NICK = 100
    val FLAG_SIGN = 101
    @BindView(R.id.title)
    lateinit var userName: TextView
    @BindView(R.id.opera)
    lateinit var url: TextView
    @BindView(R.id.nickName)
    lateinit var nickName: TextView
    @BindView(R.id.sign)
    lateinit var sign: TextView
    @BindView(R.id.d2img)
    lateinit var d2Img: ImageView
    lateinit var alertContent: EditText

    private lateinit var user: UserBean

    override fun getLayoutId(): Int {
        return R.layout.fragment_mine
    }

    override fun initView() {
        super.initView()
        userName.text = getString(R.string.mine_title)
        url.text = getString(R.string.setConnection)
        initData()
    }

    override fun initData() {
        if(mContext is AbsParentBaseActivity){
            user = (mContext as AbsParentBaseActivity).getUser()
            nickName.text = if(TextUtils.isEmpty(user.nickName)) user.loginName else user.nickName
            sign.text = user.sign
            create2d()
        }
    }

    @OnClick(R.id.opera, R.id.nickName, R.id.sign)
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.opera->{
                startActivity(Intent(mContext, ServiceAddressActivity::class.java))
            }
            R.id.nickName->{
                (activity as BaseActivity).showDialog(title = getString(R.string.mine_nick),
                    contentId = R.layout.show_alert_input, type = FLAG_NICK)
            }
            R.id.sign->{
                (activity as BaseActivity).showDialog(title = getString(R.string.mine_sign),
                    contentId = R.layout.show_alert_input, type = FLAG_SIGN)
            }
            R.id.double_sure->{
                val type = v.contentDescription.toString().toInt()
                if(type == FLAG_NICK){
                    if(nickName.text.toString() != alertContent.text.toString()){
                        nickName.text = alertContent.text
                        user.nickName = nickName.text.toString().trim()
                        create2d()
                        saveData(user)
                    }
                }else if(type == FLAG_SIGN){
                    if(sign.text.toString() != alertContent.text.toString()){
                        sign.text = alertContent.text
                        user.sign = sign.text.toString().trim()
                        saveData(user)
                    }
                }
            }
            else -> super.onClick(v)
        }
    }

    override fun onAlertView(v: View, type: Int) {
        when(type){
            FLAG_NICK->{
                setAlertHint(v, getString(R.string.mine_nick), nickName.text.toString())
            }
            FLAG_SIGN->{
                setAlertHint(v, getString(R.string.mine_sign), sign.text.toString())
            }
            else->super.onAlertView(v, type)
        }
    }

    private fun create2d(){
        val width = resources.getDimensionPixelSize(R.dimen.dp_360)
        val hints = mutableMapOf(
            EncodeHintType.CHARACTER_SET to "utf-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            EncodeHintType.MARGIN to "0")
//        val msg = """{"userName":"${user.loginName}","nickName":"${user.nickName}","address":"${user.address}"}"""
        val bit = BitMatrix2BitmapUtil().createQRCode(context = user.address,
            hints= hints, point = Point(width, width))
        d2Img.setImageBitmap(bit)
    }

    private fun setAlertHint(v: View, hint: String, text: String?=""){
        v.findViewById<TextView>(R.id.inputHint).text =
            getString(R.string.update_hint, hint)
        alertContent = v.findViewById(R.id.input)
        alertContent.hint = getString(R.string.input_hint, hint)
        alertContent.setText(text)
    }

    private fun saveData(user: UserBean){
        //存缓存
        context!!.getSharedPreferences(Constant.configFileName, Context.MODE_PRIVATE)
            .edit().putString(Constant.CURRENT_ACCOUNT, Gson().toJson(user))
            .apply()
        //数据库
        LocalDBManager(context!!).getTableOperation(UserOperaDao::class.java)
            .update(user.nickName, user.sign, user.loginId, user.address)
    }
}