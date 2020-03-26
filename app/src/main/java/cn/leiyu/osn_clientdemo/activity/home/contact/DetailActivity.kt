package cn.leiyu.osn_clientdemo.activity.home.contact

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import butterknife.BindView
import butterknife.OnClick
import cn.leiyu.base.utils.AddressUtil
import cn.leiyu.osn_clientdemo.R
import cn.leiyu.osn_clientdemo.activity.SubBaseActivity
import cn.leiyu.osn_clientdemo.activity.home.msg.SendActivity
import cn.leiyu.osn_clientdemo.beans.MsgBean
import cn.leiyu.osn_clientdemo.beans.UserBean

/**
 * 通讯录-详细
 */
class DetailActivity: SubBaseActivity() {
    @BindView(R.id.head)
    lateinit var head: TextView
    @BindView(R.id.nickName)
    lateinit var nickName: TextView
    @BindView(R.id.address)
    lateinit var address: TextView
    @BindView(R.id.remark)
    lateinit var remark: TextView
    private var mBean: UserBean? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_contactdetail
    }

    override fun initView() {
        topTitle.text = getString(R.string.contact_title)
    }

    override fun initData() {
        mBean = intent.getParcelableExtra("item")
        mBean?.let {
            if(TextUtils.isEmpty(it.nickName))it.nickName = " "
            head.text = it.nickName.substring(it.nickName.length - 1, it.nickName.length)
            val shape = resources.getDrawable(R.drawable.bg_circle) as GradientDrawable
            shape.setColor(it.lableColor)
            head.setBackgroundDrawable(shape)
            nickName.text = it.nickName
            address.text = it.address
            remark.text = it.remark
        }
    }

    @OnClick(R.id.sendMsg)
    override fun onClick(v: View?) {
        if(v?.id == R.id.sendMsg){
            val bean = MsgBean(_id = 0, sendId = getUser()._id.toInt(), peerId = mBean?._id!!.toInt(),
                peerName = mBean?.nickName!!, peerAddress = mBean?.address!!,
                msg = "", time = "", unRead = 0, peerLableColor = mBean?.lableColor!!)
            startActivity(Intent(this, SendActivity::class.java)
                .putExtra("bean", bean))
            setResult(Activity.RESULT_OK)
            finish()
        }
        else super.onClick(v)
    }
}