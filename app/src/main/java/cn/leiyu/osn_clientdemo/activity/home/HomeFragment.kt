package cn.leiyu.osn_clientdemo.activity.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ListView
import android.widget.TextView
import butterknife.BindView
import butterknife.OnClick
import butterknife.OnItemClick
import cn.leiyu.base.utils.AddressUtil
import cn.leiyu.base.utils.BaseRefreshUtil
import cn.leiyu.base.utils.encoded.EcKeyUtils
import cn.leiyu.base.utils.encoded.EcSignUtil
import cn.leiyu.osn_clientdemo.MainActivity
import cn.leiyu.osn_clientdemo.R
import cn.leiyu.osn_clientdemo.activity.AbsParentBaseActivity
import cn.leiyu.osn_clientdemo.activity.SubBaseFragment
import cn.leiyu.osn_clientdemo.activity.home.contact.AddContactActivity
import cn.leiyu.osn_clientdemo.activity.home.contact.DetailActivity
import cn.leiyu.osn_clientdemo.adapters.ContactAdapter
import cn.leiyu.osn_clientdemo.beans.UserBean
import cn.leiyu.osn_clientdemo.db.LocalDBManager
import cn.leiyu.osn_clientdemo.db.tables.UserOperaDao
import com.chanven.lib.cptr.PtrClassicFrameLayout


/**
 * 主界面 - 通讯录布局
 */
class HomeFragment : SubBaseFragment(), BaseRefreshUtil.IRefreshCallback<HomeFragment> {

    @BindView(R.id.title)
    lateinit var title: TextView
    @BindView(R.id.opera)
    lateinit var addFriend: TextView
    @BindView(R.id.opera1)
    lateinit var addGroup: TextView
    @BindView(android.R.id.list)
    lateinit var listView: ListView
    @BindView(R.id.refresh_view)
    lateinit var refreshView: PtrClassicFrameLayout
    private lateinit var refreshUtil: BaseRefreshUtil<HomeFragment>
    //private lateinit var user: UserBean
    private lateinit var userOperaDao: UserOperaDao
    private var adapter: ContactAdapter? = null
    private var page: Int = 1

    companion object{
        var gPrivateKey = ""//byteArrayOf()
        lateinit var user:UserBean
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_home
    }

    override fun initView() {
        super.initView()
        title.text = getString(R.string.contact_title)
        addFriend.text = getString(R.string.contact_add)
        addGroup.text = "添加群组"
        refreshUtil = BaseRefreshUtil(this, refreshView)
        refreshUtil.initView()
        initData()
    }

    override fun initData() {
        if(mContext is AbsParentBaseActivity) {
            user = (mContext as AbsParentBaseActivity).getUser()
        }
        val addressUtil = AddressUtil(activity?.filesDir.toString())
        gPrivateKey = addressUtil.getPrivateKey("", user.address)
        userOperaDao = LocalDBManager(mContext!!).getTableOperation(UserOperaDao::class.java)
        readUser()

//        val publicKey = EcKeyUtils.getPulicKeyFromAddressNew(user.address)
//        val privateKey = EcKeyUtils.getEcPrivateKeyFromHex(gPrivateKey)
//        val testData = "this is a test";
//        val sign = EcSignUtil.signData(testData.toByteArray(), privateKey)
//        val flag = EcSignUtil.verifySign(testData.toByteArray(), publicKey, sign)
//        val ecData = EcKeyUtils.ECIESEncrypt(publicKey, testData.toByteArray())
//        val dcData = EcKeyUtils.ECIESDecrypt(privateKey, ecData)
//        val rData = String(dcData)
//        val encData = EcKeyUtils.ECEncrypt(publicKey, testData.toByteArray())
//        val decData = EcKeyUtils.ECDecrypt(privateKey, encData)
    }

    @OnItemClick(android.R.id.list)
    fun onItemClick(position: Int){
        activity?.startActivityForResult(Intent(mContext!!, DetailActivity::class.java)
            .putExtra("item", adapter?.getItem(position) as UserBean),
            (activity as? MainActivity)!!.FLAG_SEND)
    }

    @OnClick(R.id.opera, R.id.opera1)
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.opera-> {
                startActivityForResult(
                    Intent(mContext!!, AddContactActivity::class.java),
                    102
                )
            }
            R.id.opera1->{
                val intent = Intent(mContext!!, AddContactActivity::class.java)
                intent.putExtra("data", "group")
                startActivityForResult(intent,103)
            }
            else-> super.onClick(v)
        }
    }

    override fun onPullDownRefresh() {
        page = 1
        readUser()
        refreshUtil.resetRefresh(page)
    }

    override fun onPullUpRefresh() {
        page++
        readUser()
        refreshUtil.resetRefresh(page)
    }

    override fun getRefreshUtil(): BaseRefreshUtil<HomeFragment> {
        return refreshUtil
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if((requestCode == 102 || requestCode == 103) && resultCode == Activity.RESULT_OK){
            //重新加载
            onPullDownRefresh()
        }else
            super.onActivityResult(requestCode, resultCode, data)
    }

    private fun readUser(){
        val users = userOperaDao.query(user, page)
        if(adapter == null || page == 1){
            adapter = ContactAdapter(mContext!!, users)
            listView.adapter = adapter
        }else adapter?.addData(users, page == 1)
    }
}