package cn.leiyu.osn_clientdemo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.util.JsonReader
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import cn.leiyu.base.http.JsonVolleyUtil
import cn.leiyu.base.http.VolleyListenerInterface
import cn.leiyu.base.utils.LogUtil
import cn.leiyu.base.utils.encoded.EcKeyUtils
import cn.leiyu.base.weak.WeakHandlerCallback
import cn.leiyu.osn_clientdemo.activity.AbsParentBaseActivity
import cn.leiyu.osn_clientdemo.activity.home.HomeFragment
import cn.leiyu.osn_clientdemo.activity.home.LoginLogFragment
import cn.leiyu.osn_clientdemo.activity.home.MineFragment
import cn.leiyu.osn_clientdemo.adapters.HomePageAdapter
import cn.leiyu.osn_clientdemo.beans.UserBean
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.Socket

/**
 * 程序主页
 */
class MainActivity : AbsParentBaseActivity(), ViewPager.OnPageChangeListener
    , IRefreshMsgCallback{

    /**
     * 发送标志
     */
    internal val FLAG_SEND = 1001
    lateinit var viewPager: ViewPager
    lateinit var homeNav: RadioGroup
    private lateinit var heart: Handler
    var isLogin:Boolean = false
    lateinit var userBead:UserBean

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        viewPager = findViewById(R.id.home_vp)
        viewPager.addOnPageChangeListener(this)
        val data = listOf<Fragment>(LoginLogFragment(), HomeFragment(), MineFragment())
        viewPager.adapter = HomePageAdapter(data, supportFragmentManager)
        homeNav = findViewById(R.id.home_nav)
        homeNav.setOnCheckedChangeListener { _, id ->
            when(id){
                R.id.rb_home->viewPager.currentItem = 0
                R.id.rb_loginLog->viewPager.currentItem = 1
                R.id.rb_mine->viewPager.currentItem = 2
            }
        }
        IMApp.refreshMsgCallback = this
        userBead = getUser()
    }

    override fun initData() {
        Constant.API.SERVICE_HOST = getSharedPreferences(Constant.configFileName, Context.MODE_PRIVATE)
            .getString(Constant.SERVICE_API, "")!!

        //heart = Handler(HeartHandler(this))
        IMApp.initSocket(this)
        Thread.sleep(100)
        IMApp.login(userBead)
    }

    override fun onResume() {
        super.onResume()
        if(this != IMApp.refreshMsgCallback){
            LogUtil.e(TAG!!, "回调不相同, 重新赋值")
            IMApp.refreshMsgCallback = this
        }
    }

    override fun onActivityResult(reqCode: Int, resultCode: Int, data: Intent?) {
        var requestCode = reqCode
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == FLAG_SEND && resultCode == Activity.RESULT_OK){
            viewPager.currentItem = 0
            requestCode = 102
        }
        if(resultCode == Activity.RESULT_OK){
            baseFragment?.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        val radio = homeNav.getChildAt(position) as RadioButton
        radio.isChecked = true
    }

    override fun onDestroy() {
        super.onDestroy()
        IMApp.refreshMsgCallback = null
        heart.removeCallbacksAndMessages(null)
    }

    override fun refreshMsg() {
        if(baseFragment is LoginLogFragment){
            (baseFragment as LoginLogFragment).refreshMsg()
        }
    }

    internal fun sendHeart(){
//        if(isLogin)
//            IMApp.getMessage(this, this, userBead)
//        else
//            IMApp.login(this, this, userBead)
    }

    inner class HeartHandler(context: MainActivity): WeakHandlerCallback<MainActivity>(context){
        override fun handleMessage(msg: Message): Boolean {
            var result = false
            when(msg.what){
                10-> {
                    getWeakContext()?.sendHeart()
                    result = true
                }
                11->{
                    //IMApp.getMessage(getWeakContext()!!, getWeakContext()!!, userBead)
                    result = true
                }
            }
            return result
        }
    }
    override fun updateView(){

    }
}
