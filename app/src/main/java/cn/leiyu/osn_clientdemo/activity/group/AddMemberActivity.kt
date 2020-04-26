package cn.leiyu.osn_clientdemo.activity.group

import android.app.Activity
import android.view.View
import android.widget.ListView
import butterknife.BindView
import butterknife.OnClick
import cn.leiyu.base.http.VolleyListenerInterface
import cn.leiyu.osn_clientdemo.IMApp
import cn.leiyu.osn_clientdemo.R
import cn.leiyu.osn_clientdemo.activity.SubBaseActivity
import cn.leiyu.osn_clientdemo.activity.home.HomeFragment
import cn.leiyu.osn_clientdemo.adapters.ContactAdapter
import cn.leiyu.osn_clientdemo.db.LocalDBManager
import cn.leiyu.osn_clientdemo.db.tables.GroupOperaDao
import cn.leiyu.osn_clientdemo.db.tables.UserOperaDao
import org.json.JSONObject

class AddMemberActivity : SubBaseActivity(){
    private var adapter: ContactAdapter? = null
    private lateinit var userOperaDao: UserOperaDao
    private var groupID: String? = null
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
                val member = ArrayList<JSONObject>()
                for(i in adapter!!.selItem.indices){
                    if(adapter!!.selItem[i] == true){
                        val json = JSONObject()
                        json.put("name", adapter!!.data[i].nickName)
                        json.put("user", adapter!!.data[i].address)
                        member.add(json)
                    }
                }
                if(member.size != 0){
                    //IMApp.addMember(this, this, groupID!!, member.toTypedArray())
                    IMApp.addMember(groupID!!, member.toTypedArray())
                }
                setResult(Activity.RESULT_OK)
                finish()
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
}