package cn.leiyu.osn_clientdemo.activity.home.contact

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.SQLException
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import butterknife.BindView
import butterknife.OnClick
import cn.leiyu.base.http.VolleyListenerInterface
import cn.leiyu.base.utils.AddressUtil
import cn.leiyu.base.utils.LogUtil
import cn.leiyu.osn_clientdemo.IMApp
import cn.leiyu.osn_clientdemo.R
import cn.leiyu.osn_clientdemo.activity.SubBaseActivity
import cn.leiyu.osn_clientdemo.activity.home.HomeFragment
import cn.leiyu.osn_clientdemo.beans.GroupBean
import cn.leiyu.osn_clientdemo.beans.UserBean
import cn.leiyu.osn_clientdemo.db.LocalDBManager
import cn.leiyu.osn_clientdemo.db.tables.GroupOperaDao
import cn.leiyu.osn_clientdemo.db.tables.UserOperaDao
import cn.leiyu.osn_clientdemo.utils.ProductLableUtil
import com.google.zxing.CaptureActivity

/**
 * 添加联系人
 */
open class AddContactActivity: SubBaseActivity() {

    @BindView(R.id.friendId)
    lateinit var friendId: EditText
    @BindView(R.id.friendNick)
    lateinit var friendNick: EditText
    @BindView(R.id.remark)
    lateinit var remark: EditText
    @BindView(R.id.scanFriend)
    lateinit var scan: ImageView
    //是否添加成功
    protected var isSuccess = false
    private val addType: String ? by lazy { intent.getStringExtra("data") }
    private var id: Array<String>? = null
    lateinit var addBean:UserBean

    override fun getLayoutId(): Int {
        return R.layout.activity_addcontact
    }

    override fun initView() {
        topTitle.text = getString(R.string.contact_add)
        topMenu.text = getString(R.string.confirm)
        if(addType != null && addType.equals("group")) {
            topTitle.text = "添加群组"
            val addressUtil = AddressUtil(this.filesDir.absolutePath)
            id = addressUtil.genGroupID(addressUtil.createWord(), "group")
            friendId.setText(id?.get(0))
            scan.visibility = View.INVISIBLE
        }
    }

    override fun initData() {
    }

    @OnClick(R.id.scanFriend)
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.scanFriend->{
                v.isEnabled = false
                startActivityForResult(Intent(this, CaptureActivity::class.java),
                    101)
            }
            R.id.toolbar_menu->{
                try {
                    if(addType != null && addType.equals("group"))
                        checkGroup(v)
                    else
                        checkFriend(v)
                }catch (e: Exception){
                    e.printStackTrace()
                    v.isEnabled = true
                    LogUtil.e(TAG!!, "检查信息异常 ${e.localizedMessage}")
                }
            }
            else-> super.onClick(v)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 101){
            findViewById<View>(R.id.scanFriend).isEnabled = true
            if(resultCode == Activity.RESULT_OK){
                //解析二维码字段 并显示
//                val json = Gson().fromJson<UserBean>(data?.getStringExtra(CaptureActivity.INTENT_EXTRA_KEY_QR_SCAN),
//                    UserBean::class.java)
                friendId.setText(data?.getStringExtra(CaptureActivity.INTENT_EXTRA_KEY_QR_SCAN))
//                friendNick.setText(if(TextUtils.isEmpty(json.nickName)) json.loginName else json.nickName)
            }
        }
    }

    override fun onBackPressed() {
        if(isSuccess)setResult(Activity.RESULT_OK)
        super.onBackPressed()
    }

    protected open fun checkGroup(v: View){
        if(IMApp.serviceID == ""){
            showToast("服务号为空")
        }
        else {
            val login = getUser()
            addBean = UserBean(
                loginId = login.loginId,
                loginName = "",
                address = HomeFragment.user.address,
                nickName = friendNick.text.toString().trim(),
                remark = remark.text.toString().trim(),
                lableColor = ProductLableUtil.getLableColor(),
                isTemp = 0
            )
            //IMApp.addGroup(this, this, addBean, id!!)
            IMApp.addGroup(addBean.nickName, id!![0], id!![1], id!![2], id!![3])
        }
        setResult(Activity.RESULT_OK)
        finish()
    }

    protected open fun checkFriend(v: View){
        //ID必传
        val params = arrayOfNulls<String>(3)
        params[0] = friendId.text.toString().trim()
        if (TextUtils.isEmpty(params[0]) && addType == null) {
            showToast(getString(R.string.input_hint, getString(R.string.contact_id)))
            return
        }
        params[1] = friendNick.text.toString().trim()
        if(TextUtils.isEmpty(params[1])){
            params[1] = " "
        }
        params[2] = remark.text.toString().trim()
        if(TextUtils.isEmpty(params[2]))params[2] = ""
        v.isEnabled = false
        //保存用户
        val login = getUser()
        val bean = UserBean(loginId = login.loginId, loginName = "",
            address = params[0]!!, nickName = params[1]!!, remark = params[2],
            lableColor = ProductLableUtil.getLableColor(), isTemp = 0)
        val hintId = try{
                //存库
            LocalDBManager(this).getTableOperation(UserOperaDao::class.java).insertUser(bean)
            //返回是更新界面
            isSuccess = true
            //重置信息
            friendId.setText("")
            friendNick.setText("")
            remark.setText("")
            R.string.success_hint
        }catch (e: SQLException){
            e.printStackTrace()
            LogUtil.e(TAG!!, "添加联系人异常 ${e.localizedMessage}")
            R.string.failed_hint
        }
        //解禁 给出提示
        v.isEnabled = true
        showToast(getString(hintId, getString(R.string.contact_add)))
        setResult(Activity.RESULT_OK, null)
        finish()
    }
}