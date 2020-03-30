package cn.leiyu.osn_clientdemo

import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import cn.leiyu.base.App
import cn.leiyu.base.http.JsonVolleyUtil
import cn.leiyu.base.http.VolleyListenerInterface
import cn.leiyu.base.utils.LogUtil
import cn.leiyu.base.utils.encoded.EcKeyUtils
import cn.leiyu.base.utils.encoded.EcSignUtil
import cn.leiyu.osn_clientdemo.activity.home.HomeFragment
import cn.leiyu.osn_clientdemo.beans.MsgBean
import cn.leiyu.osn_clientdemo.beans.MsgInterBean
import cn.leiyu.osn_clientdemo.beans.MsgInterItemBean
import cn.leiyu.osn_clientdemo.beans.UserBean
import cn.leiyu.osn_clientdemo.db.LocalDBManager
import cn.leiyu.osn_clientdemo.db.tables.GroupOperaDao
import cn.leiyu.osn_clientdemo.db.tables.MsgOperaDao
import cn.leiyu.osn_clientdemo.db.tables.UserOperaDao
import cn.leiyu.osn_clientdemo.utils.Base58
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

/**
 * 应用主程序
 */
class IMApp: App() {

    companion object Sub{
        @JvmStatic
        var refreshMsgCallback: IRefreshMsgCallback? = null
        var serviceID: String = ""

        private fun recordMessage(userBean: UserBean, msgItem: MsgInterItemBean){
            try {
                val db = LocalDBManager(getAppContext())
                //查询是否是在通讯录范围
                val users = db.getTableOperation(UserOperaDao::class.java).query(arrayOf("_id"),
                    "address = ? and loginId = ?",
                    arrayOf(msgItem.from, "${userBean.loginId}"))
                if(users == null || users.isEmpty()){
                    //用户不再通讯范围 可能是好友
                    LogUtil.e("IRefreshMsgCallback", "消息库操作 ${msgItem.from} 用户不在通讯录，请谨慎添加！ ")
                }else{
                    val privateKey = EcKeyUtils.getEcPrivateKeyFromHex(HomeFragment.gPrivateKey)
                    val msg = EcKeyUtils.ECDecrypt(privateKey, msgItem.content)

                    //在通讯录范围
                    val cv = ContentValues(5)
                    cv.put("sendId", users[0][0])
                    cv.put("peerId", userBean._id)
                    //cv.put("msg", bean.content)
                    cv.put("msg", msg)
                    cv.put("time", msgItem.timestamp)
                    cv.put("isRead", 1)
                    cv.put("sendSuccess", 1)
                    db.getTableOperation(MsgOperaDao::class.java)
                        .insert(null, cv, SQLiteDatabase.CONFLICT_ABORT)
                }
            }catch (e: SQLException){
                e.printStackTrace()
                LogUtil.e("IRefreshMsgCallback", "消息库操作异常 ${msgItem.to}  ${e.localizedMessage}")
            }
        }
        private fun completeMessage(context: Context, from:String, to:String, hash:String){
            val sign = EcKeyUtils.signOsnData(HomeFragment.gPrivateKey, hash.toByteArray())

            JsonVolleyUtil.request(context,
                Constant.API.SERVICE_HOST,
                "complete", """{"command":"complete","from":"${from}","to":"${to}", "hash":"${hash}", "sign":"${sign}"}""",
                object : VolleyListenerInterface("complete", context, mListener, mErrorListener) {
                    override fun onMySuccess(result: String?) {
                    }
                    override fun onMyError(error: VolleyError?) {
                    }
                }, false)
        }
        private fun updateGroup(context: Context, bean:MsgInterItemBean){
            val data = EcKeyUtils.ECDecrypt(HomeFragment.gPrivateKey, bean.content)
            val json:JSONObject = JSONObject(data)
            if(json.has("userList")){
                LocalDBManager(context).getTableOperation(
                    GroupOperaDao::class.java).updateUserList(bean.from, json.getString("userList"))
            }
        }
        fun login(cb:IRequestCallback, context: Context, userBean: UserBean){
            JsonVolleyUtil.request(context, Constant.API.SERVICE_HOST, "login",
                """{"command":"login", "user":"${userBean.address}"}""",
                object: VolleyListenerInterface("login", context, mListener, mErrorListener){
                    override fun onMySuccess(result: String?) {
                        serviceID = json.getString("serviceID")
                        cb.reqResult(this)
                    }
                    override fun onMyError(error: VolleyError?) {
                        cb.reqResult(this)
                    }
                }, false)
        }
        fun getMessage(cb:IRequestCallback, context: Context, userBean: UserBean){
            JsonVolleyUtil.request(context,
                Constant.API.SERVICE_HOST,
                "getMsg", """{"command":"getmsg","user":"${userBean.address}"}""",
                object : VolleyListenerInterface("getmsg", context, mListener, mErrorListener){
                    override fun onMySuccess(result: String?) {
                        try {
                            val tmp = Gson().fromJson(result,
                                object : TypeToken<MsgInterBean>() {}.type
                            ) as MsgInterBean
                            if (tmp.errCode.equals("success") && tmp.data.isNotEmpty()) {
                                //var hashList = JSONArray()
                                //var from:String? = null
                                for (bean in tmp.data) {
                                    when (bean.command) {
                                        "message" -> {
                                            recordMessage(userBean, bean)
                                            completeMessage(context, bean.from, bean.to, bean.hash)
//                                        if(from == null || bean.from.equals(from))
//                                            hashList.put(bean.hash)
//                                        else if(hashList.length() != 0){
//                                            completeMessage(context, from, userBean.address, hashList)
//                                            from = null
//                                            hashList = JSONArray()
//                                        }
                                        }
                                        "groupupdate" -> {
                                            updateGroup(context, bean)
                                        }
                                    }
                                }
                                refreshMsgCallback?.refreshMsg()
                            }
                        }catch (e:Exception){
                            this.result = false
                        }
                        cb.reqResult(this)
                    }
                    override fun onMyError(error: VolleyError?) {
                        cb.reqResult(this)
                    }
                },
                false)
        }
        fun sendMessage(cb:IRequestCallback, context: Context, id:Long, userBean:UserBean, msgBean: MsgBean, time:String, msg:String){
            val encMsg = EcKeyUtils.ECEncrypt(msgBean.peerAddress, msg.toByteArray())
            val sb = StringBuffer();
            sb.append(userBean.address);
            sb.append(msgBean.peerAddress);
            sb.append(encMsg);
            sb.append(time);
            val hash = EcKeyUtils.hashOsnData(sb.toString().toByteArray())
            val sign = EcKeyUtils.signOsnData(HomeFragment.gPrivateKey, sb.toString().toByteArray())

            JsonVolleyUtil.request(context, Constant.API.SERVICE_HOST, "sendMsg",
                """{"command":"message", "from":"${userBean.address}",
                |"to":"${msgBean.peerAddress}","timestamp":"$time","crypto":"no","description":"test",
                |"content":"$encMsg","sign":"${sign}", "hash":"${hash}"}""".trimMargin(),
                object: VolleyListenerInterface("message", context, id, mListener, mErrorListener){
                    override fun onMySuccess(result: String?) {
                        cb.reqResult(this)
                    }

                    override fun onMyError(error: VolleyError?) {
                        cb.reqResult(this)
                    }
                }, false)
        }
        fun addGroup(cb:IRequestCallback, context: Context, bean: UserBean, groupInfo: Array<String>) {
            val groupID = groupInfo[0]
            val groupPrivateKey = groupInfo[1]
            val gsShadowPubKey = groupInfo[2]
            val gsShadowPriKey = groupInfo[3]
            val data = """{"group":"${groupID}", "privateKey":"${groupPrivateKey}", "shadowKey":"${gsShadowPubKey}"}"""
            val encData = EcKeyUtils.ECEncrypt(serviceID, data.toByteArray())
            val signData = EcKeyUtils.signOsnData(HomeFragment.gPrivateKey, encData.toByteArray())
            JsonVolleyUtil.request(context,
                Constant.API.SERVICE_HOST,
                "creategroup", """{"command":"creategroup", "owner":"${bean.address}", 
                    |"data":"${encData}", "sign":"${signData}"}""".trimMargin(),
                object : VolleyListenerInterface("addgroup", context, mListener, mErrorListener) {
                    override fun onMySuccess(result: String?) {
                        cb.reqResult(this)
                    }

                    override fun onMyError(error: VolleyError?) {
                        cb.reqResult(this)
                    }
                }, false
            )
        }
        fun addMember(cb:IRequestCallback, context: Context, groupID:String, memberInfo: Array<String>){
            val data = JSONObject()
            val list = JSONArray()
            data.put("userList", list)
            for(user in memberInfo)
                list.put(user)
            val encData = EcKeyUtils.ECEncrypt(groupID, data.toString().toByteArray())
            val origData = EcKeyUtils.ECDecrypt("dd632e9d13091282e5a647224110744debadd17f9aded9393abbee983dc697bb", encData)
            val signData = EcKeyUtils.signOsnData(HomeFragment.gPrivateKey, encData.toByteArray())
            val json = """{"command":"addmember", "user":"${HomeFragment.user.address}", 
                |"group":"${groupID}", "data":"${encData}", "sign":"${signData}"}""".trimMargin()
            JsonVolleyUtil.request(context,
                Constant.API.SERVICE_HOST,
                "creategroup", json,
                object : VolleyListenerInterface("addmember", context, mListener, mErrorListener) {
                    override fun onMySuccess(result: String?) {
                        cb.reqResult(this)
                    }
                    override fun onMyError(error: VolleyError?) {
                        cb.reqResult(this)
                    }
                }, false
            )
        }
    }
}
interface IRequestCallback{
    fun reqResult(volley:VolleyListenerInterface)
}

/**
 * 消息回调接口
 */
interface IRefreshMsgCallback{


    /**
     * 刷新消息
     */
    fun refreshMsg(){}
}