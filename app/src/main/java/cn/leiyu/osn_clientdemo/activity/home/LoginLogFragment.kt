package cn.leiyu.osn_clientdemo.activity.home

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.ListView
import android.widget.TextView
import butterknife.BindView
import butterknife.OnItemClick
import cn.leiyu.base.utils.BaseRefreshUtil
import cn.leiyu.osn_clientdemo.Constant
import cn.leiyu.osn_clientdemo.MainActivity
import cn.leiyu.osn_clientdemo.R
import cn.leiyu.osn_clientdemo.activity.SubBaseFragment
import cn.leiyu.osn_clientdemo.activity.home.msg.SendActivity
import cn.leiyu.osn_clientdemo.activity.mine.ServiceAddressActivity
import cn.leiyu.osn_clientdemo.adapters.MsgAdapter
import cn.leiyu.osn_clientdemo.beans.MsgBean
import cn.leiyu.osn_clientdemo.beans.UserBean
import cn.leiyu.osn_clientdemo.db.LocalDBManager
import cn.leiyu.osn_clientdemo.db.tables.MsgOperaDao
import com.chanven.lib.cptr.PtrClassicFrameLayout

/**
 * 主界面 - 消息布局
 */
class LoginLogFragment : SubBaseFragment(), BaseRefreshUtil.IRefreshCallback<LoginLogFragment>{
    @BindView(R.id.title)
    lateinit var title: TextView
    @BindView(R.id.opera)
    lateinit var opera: TextView
    @BindView(android.R.id.list)
    lateinit var listView: ListView
    @BindView(R.id.refresh_view)
    lateinit var refreshView: PtrClassicFrameLayout
    private lateinit var refreshUtil: BaseRefreshUtil<LoginLogFragment>
    private var adapter: MsgAdapter? = null
    private var msgOperaDao: MsgOperaDao? = null
    private var page = 1
    private var user: UserBean? = null
//    private var isRefreshUrl = false

    override fun getLayoutId(): Int {
        return R.layout.fragment_msg
    }

    override fun initView() {
        super.initView()
        title.text = getString(R.string.msg_title)
        opera.visibility = View.INVISIBLE
        refreshUtil = BaseRefreshUtil(this, refreshView)
        refreshUtil.initView()
        initData()
    }

    override fun initData() {
        if(mContext is MainActivity){
            val tmp = mContext as MainActivity
            user = tmp.getUser()
            if(Constant.API.SERVICE_HOST == ""){
                //设置访问地址
                startActivityForResult(Intent(mContext, ServiceAddressActivity::class.java),
                    101)
            }else{
                if(msgOperaDao == null){
                    tmp.sendHeart()
                }
                queryLocalData(page)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 101 && resultCode == Activity.RESULT_OK){
            //重新获取信息
            page = 1
            if(mContext is MainActivity){
                (mContext as MainActivity).sendHeart()
            }
        }else if(requestCode == 102){
            refreshMsg()
        }else
            super.onActivityResult(requestCode, resultCode, data)
    }

    @OnItemClick(android.R.id.list)
    fun onItemClick(position: Int){
        startActivityForResult(Intent(mContext, SendActivity::class.java)
            .putExtra("bean", listView.adapter.getItem(position) as MsgBean), 102)
    }

    override fun onPullDownRefresh() {
        refreshView.refreshComplete()
//        if(isRefreshUrl){
//            isRefreshUrl = false
//        }
    }

    override fun onPullUpRefresh() {
        page++
        //加载本地数据
        queryLocalData(page)
    }

    override fun getRefreshUtil(): BaseRefreshUtil<LoginLogFragment> {
        return refreshUtil
    }

    fun refreshMsg(){
        page = 1
        queryLocalData(page)
    }


    private fun queryLocalData(page: Int){
        if(msgOperaDao == null)
            msgOperaDao = LocalDBManager(mContext!!).getTableOperation(MsgOperaDao::class.java)
        val msg = msgOperaDao?.query(user?._id!!, page)
        if(adapter == null){
            adapter = MsgAdapter(mContext!!, msg!!)
        }else
            adapter?.addData(msg!!, page == 1)
        if(listView.adapter == null)listView.adapter = adapter
    }
}