package cn.leiyu.osn_clientdemo.activity.group

import android.app.Activity
import android.view.View
import android.widget.ListView
import butterknife.BindView
import butterknife.OnClick
import cn.leiyu.base.http.VolleyListenerInterface
import cn.leiyu.osn_clientdemo.IMApp
import cn.leiyu.osn_clientdemo.IRequestCallback
import cn.leiyu.osn_clientdemo.R
import cn.leiyu.osn_clientdemo.activity.SubBaseActivity
import cn.leiyu.osn_clientdemo.activity.home.HomeFragment
import cn.leiyu.osn_clientdemo.adapters.ContactAdapter
import cn.leiyu.osn_clientdemo.db.LocalDBManager
import cn.leiyu.osn_clientdemo.db.tables.GroupOperaDao
import cn.leiyu.osn_clientdemo.db.tables.UserOperaDao

class AddMemberActivity : SubBaseActivity(), IRequestCallback{
    private var adapter: ContactAdapter? = null
    private lateinit var userOperaDao: UserOperaDao
    private var groupID: String? = null
    var member = ArrayList<String>()
    @BindView(android.R.id.list)
    lateinit var listView: ListView

    override fun getLayoutId(): Int {
        return R.layout.activity_addmember
    }

    override fun initView() {
        topTitle.text = "添加成员"
    }

    override fun initData() {
        userOperaDao = LocalDBManager(mContext!!).getTableOperation(UserOperaDao::class.java)
        val users = userOperaDao.query(HomeFragment.user, 1)
        if(adapter == null){
            adapter = ContactAdapter(mContext!!, users, true)
            listView.adapter = adapter
        }else
            adapter?.addData(users, true)
        groupID = intent.getStringExtra("groupID");
    }

    @OnClick(R.id.ok, R.id.cancel)
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.ok->{
                member.clear()
                for(i in adapter!!.selItem.indices){
                    if(adapter!!.selItem[i] == true){
                        member.add(adapter!!.data[i].address)
                    }
                }
                if(member.size != 0){
                    IMApp.addMember(this, this, groupID!!, member.toTypedArray())
                }
            }
            R.id.cancel->{
                setResult(Activity.RESULT_OK)
                finish()
            }
            else-> super.onClick(v)
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
    }

    override fun reqResult(volley: VolleyListenerInterface) {
        if(volley.result) {
            showToast("添加成功")
//            val groupOperaDao = LocalDBManager(this).getTableOperation(GroupOperaDao::class.java);
//            val groupBean = groupOperaDao.query(groupID!!)
//            for(o in member)
//                groupBean!!.userList += o + ";"
//            groupOperaDao.update(groupBean!!)
        }
        else
            showToast("添加失败")
        setResult(Activity.RESULT_OK)
        finish()
    }
}