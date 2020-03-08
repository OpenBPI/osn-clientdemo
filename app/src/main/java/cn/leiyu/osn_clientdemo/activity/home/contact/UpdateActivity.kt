package cn.leiyu.osn_clientdemo.activity.home.contact

import android.text.TextUtils
import android.view.View
import cn.leiyu.osn_clientdemo.R
import cn.leiyu.osn_clientdemo.beans.UserBean
import cn.leiyu.osn_clientdemo.db.LocalDBManager
import cn.leiyu.osn_clientdemo.db.tables.UserOperaDao

/**
 * 通讯录-修改
 */
class UpdateActivity: AddContactActivity() {

    private lateinit var userBean: UserBean

    override fun initView() {
        super.initView()
        topTitle.text = getString(R.string.update_hint, "")
        friendId.isEnabled = false
        findViewById<View>(R.id.scanFriend).visibility = View.INVISIBLE
    }

    override fun initData() {
        super.initData()
        userBean = intent.getParcelableExtra("bean") as UserBean
        userBean.let{
            friendId.setText(it.address)
            friendNick.setText(it.nickName)
            remark.setText(it.remark)
        }
    }

    override fun checkFriend(v: View) {
        v.isEnabled = false
        userBean.nickName = friendNick.text.toString().trim()
        if(TextUtils.isEmpty(userBean.nickName)){
            userBean.nickName = ""
        }
        userBean.remark = remark.text.toString().trim()
        if(TextUtils.isEmpty(userBean.remark))userBean.remark = ""
        val result = LocalDBManager(this).getTableOperation(UserOperaDao::class.java)
            .update(userBean)
        if(result is Number && result.toInt() > 0){
            //修改成功
            isSuccess = true
            onBackPressed()
        }
        //解禁
        v.isEnabled = true
    }
}