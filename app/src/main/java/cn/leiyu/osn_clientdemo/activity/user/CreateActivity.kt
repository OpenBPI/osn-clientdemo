package cn.leiyu.osn_clientdemo.activity.user

import android.content.Intent
import android.database.SQLException
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ScrollView
import butterknife.BindView
import cn.leiyu.base.utils.AddressUtil
import cn.leiyu.base.weak.AbsWeakContext
import cn.leiyu.osn_clientdemo.LoginActivity
import cn.leiyu.osn_clientdemo.R
import cn.leiyu.osn_clientdemo.activity.SubBaseActivity
import cn.leiyu.osn_clientdemo.beans.UserBean
import cn.leiyu.osn_clientdemo.db.LocalDBManager
import cn.leiyu.osn_clientdemo.db.tables.LoginOperaDao
import cn.leiyu.osn_clientdemo.db.tables.UserOperaDao
import cn.leiyu.osn_clientdemo.utils.PatternUtil
import cn.leiyu.osn_clientdemo.utils.ProductLableUtil

/**
 * 创建身份界面
 */
class CreateActivity : SubBaseActivity() {

    @BindView(R.id.scrollView)
    lateinit var scrollView: ScrollView
    @BindView(R.id.userName)
    lateinit var userName: EditText
    @BindView(R.id.userAccount)
    lateinit var userAccount: EditText
    @BindView(R.id.userPwd)
    lateinit var userPwd: EditText
    private lateinit var dbManager: LocalDBManager
    private lateinit var loginOperaDao: LoginOperaDao

    override fun getLayoutId(): Int {
        return R.layout.activity_createuser
    }

    override fun initView() {
        topTitle.text = getString(R.string.register)
        topMenu.text = getString(R.string.confirm)
        scrollView.isFillViewport = !scrollView.arrowScroll(View.FOCUS_UP)
    }

    override fun initData() {
        dbManager = LocalDBManager(this)
        loginOperaDao = dbManager.getTableOperation(LoginOperaDao::class.java)
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.toolbar_menu) {
            checkInput()
        } else super.onClick(v)
    }

    private fun checkInput() {
        var name = userName.text.toString().trim()
        if (TextUtils.isEmpty(name)){
            showToast(getString(R.string.inputUName))
            return
        }
        if(loginOperaDao.queryUser(name).isNotEmpty()){
            //本地存在相同用户
            showToast(getString(R.string.userError1))
            return
        }
        var account = userAccount.text.toString().trim()
        val pwd = userPwd.text.toString().trim()
        if (!checkPwd(account) || !checkPwd(pwd, getString(R.string.confirmHint))){
            return
        }
        if(account != pwd){
            showToast(getString(R.string.confirmPwdError))
            return
        }
        Thread(object : Runnable, AbsWeakContext<CreateActivity>(this) {
            override fun run() {
                var words = arrayOfNulls<String>(2)
                val addressUtil = AddressUtil(getWeakContext()?.filesDir.toString())
                words[0] = addressUtil.genIdentify(addressUtil.createWord(), pwd)
                words[1] = " "//genWallet(pwd)
                runOnUiThread(Runnable {
                    if (TextUtils.isEmpty(words[0])) {
                        getWeakContext()?.showToast(getString(R.string.failed_hint, getString(R.string.register)))
                        return@Runnable
                    }
//                    if (TextUtils.isEmpty(words[1])) {
//                        getWeakContext()?.showToast("钱包助记词生成失败")
//                        return@Runnable
//                    }
                    getWeakContext()?.let {
                        val bean = UserBean(loginId = -1, loginName = name,
                            address = addressUtil.address[0]!!)
                        try{
                            val index = it.loginOperaDao.insert(bean)
                            if(index <= 0)return@Runnable
                            bean.loginId = index
                            bean.lableColor = ProductLableUtil.getLableColor()
                            bean.nickName = bean.loginName
                            it.dbManager.getTableOperation(UserOperaDao::class.java).insertUser(bean)

//                        bean.address = addressUtil.address[1]!!
//                        bean.type = 1
//                        userDao.insertUser(bean)
                            //进入登陆
                            it.startActivity(Intent(it, LoginActivity::class.java))
                            it.finish()
                        }catch (e: SQLException){
                            e.printStackTrace()
                        }
                    }
                })
            }
        }).start()
    }

    private fun checkPwd(pwd: String?, str: String=""): Boolean{
        return if (TextUtils.isEmpty(pwd)) {
            showToast("${str}密码不能为空")
            false
        } else if (pwd!!.length < 8) {
            showToast("${str}密码强度不够，至少8位")
            false
        } else if (PatternUtil.isDoubleChar(pwd)) {
            showToast("${str}密码不能含有中文字符")
            false
        } else true
    }
}