package cn.leiyu.osn_clientdemo.activity.home.contact

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.View
import android.widget.ListView
import android.widget.TextView
import butterknife.BindView
import butterknife.OnClick
import cn.leiyu.base.utils.AddressUtil
import cn.leiyu.osn_clientdemo.R
import cn.leiyu.osn_clientdemo.activity.SubBaseActivity
import cn.leiyu.osn_clientdemo.activity.group.AddMemberActivity
import cn.leiyu.osn_clientdemo.activity.home.msg.SendActivity
import cn.leiyu.osn_clientdemo.adapters.ContactAdapter
import cn.leiyu.osn_clientdemo.beans.MsgBean
import cn.leiyu.osn_clientdemo.beans.UserBean
import cn.leiyu.osn_clientdemo.db.LocalDBManager
import cn.leiyu.osn_clientdemo.db.tables.GroupOperaDao
import org.json.JSONArray

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
    @BindView(android.R.id.list)
    lateinit var listView: ListView
    @BindView(R.id.addMember)
    lateinit var btMember: TextView
    @BindView(R.id.userList)
    lateinit var txUserList: TextView
    private var mBean: UserBean? = null
    private var adapter: ContactAdapter? = null

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
        if(AddressUtil.isGroup(mBean!!.address)){
            val groupBean = LocalDBManager(this).getTableOperation(GroupOperaDao::class.java).query(mBean!!.address)
            val userList = JSONArray(groupBean!!.userList)
            val users = ArrayList<UserBean>()
            for(i in 0 until userList.length()){
                val user = userList.getJSONObject(i)
                val bean = UserBean(0, "", -1, user.getString("name"), user.getString("user"))
                users.add(bean)
            }
            adapter = ContactAdapter(mContext!!, users, false)
            listView.adapter = adapter
            txUserList.text = "成员个数 " + users.size.toString();
        }
        else {
            listView.visibility = View.INVISIBLE
            btMember.visibility = View.INVISIBLE
            txUserList.visibility = View.INVISIBLE
        }
    }

    @OnClick(R.id.sendMsg, R.id.addMember)
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
        else if(v?.id == R.id.addMember){
            startActivityForResult(Intent(this, AddMemberActivity::class.java).putExtra("groupID", address.text.toString()), 100)
            setResult(Activity.RESULT_OK)
            finish()
        }
        else super.onClick(v)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100){

        }
    }
}