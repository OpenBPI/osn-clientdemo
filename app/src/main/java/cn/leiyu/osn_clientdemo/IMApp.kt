package cn.leiyu.osn_clientdemo

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.hardware.camera2.CameraManager
import android.util.Log
import cn.leiyu.base.App
import cn.leiyu.base.http.JsonVolleyUtil
import cn.leiyu.base.http.VolleyListenerInterface
import cn.leiyu.base.utils.LogUtil
import cn.leiyu.base.utils.encoded.EcKeyUtils
import cn.leiyu.base.utils.encoded.EcSignUtil
import cn.leiyu.osn_clientdemo.activity.home.HomeFragment
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
import cn.leiyu.base.utils.encoded.EcKeyUtils.signOsnData
import cn.leiyu.base.utils.encoded.EcKeyUtils.hashOsnData
import androidx.core.content.ContextCompat.getSystemService
import cn.leiyu.base.utils.AddressUtil
import cn.leiyu.osn_clientdemo.beans.*
import cn.leiyu.osn_clientdemo.utils.ProductLableUtil
import kotlinx.android.synthetic.main.fragment_mine.view.*
import java.lang.ref.WeakReference
import androidx.core.content.ContextCompat.getSystemService
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import androidx.core.content.ContextCompat.getSystemService
import java.net.InetSocketAddress
import java.nio.file.Files.isReadable
import java.nio.channels.SelectionKey.OP_READ
import java.nio.channels.SelectionKey.OP_CONNECT
import androidx.core.content.ContextCompat.getSystemService
import cn.leiyu.base.activity.BaseActivity
import cn.leiyu.base.http.VolleyRequestUtil.context
import java.nio.channels.SelectionKey
import java.nio.channels.Selector


/**
 * 应用主程序
 */
class IMApp: App() {

    companion object Sub{
        @JvmStatic
        var refreshMsgCallback: IRefreshMsgCallback? = null
        var serviceID: String = ""
        lateinit var activityUI: WeakReference<MainActivity>
        lateinit var socket: Socket
        var isLogin:Boolean = false
        var myBean:UserBean? = null
        val commandMap: MutableMap<String, JSONObject> = mutableMapOf()
        var serverAddr:String = "127.0.0.1"
        var serverPort:Int = 8100
        var flState:Boolean = false

        private fun getServerAddr(){
            try {
                val data = Constant.API.SERVICE_HOST.split(":")
                serverAddr = data[0]
                if (data.size > 1)
                    serverPort = data[1].toInt()
            }catch (e:Exception){
                LogUtil.e("getServerAddr", e.toString())
            }
        }
        private fun setFlashLight(){
            flState = !flState
            try {
                val cm = getSystemService(activityUI.get()!!.applicationContext, CameraManager::class.java) as CameraManager
                val cid = cm.cameraIdList[0]
                cm.setTorchMode(cid, flState)
            }
            catch (e:Exception){
                e.printStackTrace()
            }
        }

        private fun completeMessage(from:String, to:String, hash:String){
            val sign = EcKeyUtils.signOsnData(HomeFragment.gPrivateKey, hash.toByteArray())

//            JsonVolleyUtil.request(context,
//                Constant.API.SERVICE_HOST,
//                "complete", """{"command":"complete","from":"${from}","to":"${to}", "hash":"${hash}", "sign":"${sign}"}""",
//                object : VolleyListenerInterface("complete", context, mListener, mErrorListener) {
//                    override fun onMySuccess(result: String?) {
//                    }
//                    override fun onMyError(error: VolleyError?) {
//                    }
//                }, false)
        }
        private fun joinGroup(groupData:JSONObject){
            try {
                val db = LocalDBManager(activityUI.get()!!)
                val groupBean = GroupBean(
                    groupData.getString("group"),
                    groupData.getString("name"),
                    groupData.getString("owner"),
                    "",
                    groupData.getString("shadowKey"),
                    "",
                    groupData.getJSONArray("userList").toString()
                )
                db.getTableOperation(GroupOperaDao::class.java).insertGroup(groupBean)
                val userBean = UserBean(
                    HomeFragment.user.loginId, "", -1, groupBean.name,
                    groupData.getString("group"), "", 0, "", ProductLableUtil.getLableColor()
                )
                db.getTableOperation(UserOperaDao::class.java).insertUser(userBean)

                val userList = groupData.getJSONArray("userList")
                for (i in 0 until userList.length()) {
                    val json = userList.getJSONObject(i)
                    userBean.isTemp = 1
                    userBean.nickName = json.getString("name")
                    userBean.address = json.getString("user")
                    db.getTableOperation(UserOperaDao::class.java)
                        .insertUser(userBean)
                }
                db.close()
            }
            catch (e:Exception){
                LogUtil.e("joinGroup", e.toString())
            }
        }
        private fun updateGroup(bean:MsgInterItemBean){
            try {
                val db = LocalDBManager(activityUI.get()!!);
                val groupOperaDao = db.getTableOperation(GroupOperaDao::class.java)
                val groupBean = groupOperaDao.query(bean.from)
                if (groupBean == null) {
                    val json = makeMessage("getgroupinfo", myBean!!.address, bean.from, JSONObject().toString())
                    sendPackage(json!!)

                    json.put("groupinfo", "joingroup")
                    commandMap.put("groupinfo", json)
                } else {
                    val content = takeMessage(bean)
                    content?.let {
                        val json = JSONObject(it)
                        if (json.has("userList")) {
                            groupOperaDao.updateUserList(bean.from, json.getString("userList"))
                        }
                    }
                }
                db.close()
            }
            catch (e:Exception){
                LogUtil.e("updateGroup", e.toString())
            }
        }
        private fun groupInfo(bean:MsgInterItemBean){
            try {
                val content = takeMessage(bean)
                content?.let {
                    val json = commandMap.get("groupinfo")
                    if (json!!.getString("groupinfo") == "joingroup") {
                        val groupData = JSONObject(it)
                        joinGroup(groupData)
                    }
                }
            }
            catch (e:Exception){
                LogUtil.e("groupInfo", e.toString())
            }
        }
        private fun takeMessage(bean:MsgInterItemBean):String?{
            try {
                val data = bean.from + bean.to + bean.content + bean.timestamp
                val hash = EcKeyUtils.hashOsnData(data.toByteArray())
                if (hash.equals(bean.hash) && EcKeyUtils.verifyOsnData(bean.from,data.toByteArray(),bean.sign)) {
                    val rawData = EcKeyUtils.ECDecrypt(HomeFragment.gPrivateKey, bean.content)
                    LogUtil.e("takeMessage", rawData.toString())
                    return rawData
                }
            }
            catch (e:Exception){
                e.printStackTrace()
            }
            return null;
        }
        private fun makeMessage(command:String, from:String, to:String, content:String,
                                time:String?=null):JSONObject?{
            try {
                var timestamp = time;
                if(timestamp == null)
                    timestamp = System.currentTimeMillis().toString()
                val encMsg = EcKeyUtils.ECEncrypt(to, content.toByteArray())
                val sb = StringBuffer();
                sb.append(from);
                sb.append(to);
                sb.append(encMsg);
                sb.append(timestamp);
                val hash = hashOsnData(sb.toString().toByteArray())
                val sign = signOsnData(HomeFragment.gPrivateKey, sb.toString().toByteArray())

                val json = JSONObject()
                json.put("command", command)
                json.put("from", from)
                json.put("to", to)
                json.put("timestamp", timestamp)
                json.put("crypt", "yes")
                json.put("description", "test")
                json.put("content", encMsg)
                json.put("sign", sign)
                json.put("hash", hash)
                return json
            }
            catch (e:Exception){
                LogUtil.e("makeMessage", e.toString())
            }
            return null
        }
        private fun recordMessage(msgItem: MsgInterItemBean){
            LogUtil.e("recordMessage", msgItem.from)
            try {
                val db = LocalDBManager(activityUI.get()!!)
                //查询是否是在通讯录范围
                val users = db.getTableOperation(UserOperaDao::class.java).query(arrayOf("_id"),
                    "address = ? and loginId = ?",
                    arrayOf(msgItem.from, "${myBean?.loginId}"))
                if(users == null || users.isEmpty()){
                    //用户不再通讯范围 可能是好友
                    LogUtil.e("IRefreshMsgCallback", "消息库操作 ${msgItem.from} 用户不在通讯录，请谨慎添加！ ")
                }else{
                    var msg = takeMessage(msgItem)
                    msg?.let {
                        //在通讯录范围
                        val cv = ContentValues(5)
                        if(AddressUtil.isGroup(msgItem.from)){
                            val msgGroup = JSONObject(it)
                            msg = msgGroup.getString("content")
                            val user = msgGroup.getString("from")
                            val uid = db.getTableOperation(UserOperaDao::class.java).query(arrayOf("_id"),
                                "address = ? and loginId = ?",
                                arrayOf(user, "${myBean?.loginId}"))
                            cv.put("sendId", uid[0][0])
                            cv.put("peerId", users[0][0])
                        }
                        else {
                            val msgUser = JSONObject(it)
                            msg = msgUser.getString("content");
                            cv.put("sendId", users[0][0])
                            cv.put("peerId", myBean?._id)
                        }
                        //cv.put("msg", bean.content)
                        cv.put("msg", msg)
                        cv.put("time", msgItem.timestamp)
                        cv.put("isRead", 1)
                        cv.put("sendSuccess", 1)
                        db.getTableOperation(MsgOperaDao::class.java)
                            .insert(null, cv, SQLiteDatabase.CONFLICT_ABORT)
                        //refreshMsgCallback?.refreshMsg()
                        BaseActivity.updateNotify()
                        if(msg == "click")
                            setFlashLight()
                    }
                }
                db.close()
            }catch (e: SQLException){
                e.printStackTrace()
                LogUtil.e("IRefreshMsgCallback", "消息库操作异常 ${msgItem.to}  ${e.localizedMessage}")
            }
        }

        private fun handleCreateGroup(json:JSONObject){
            try {
                val data = commandMap.get("creategroup")
                if (data == null) {
                    LogUtil.e("handleMessage", "recv unknow command: creategroup")
                } else {
                    commandMap.remove("creategroup")
                    val userBean = UserBean(
                        loginId = myBean!!.loginId,
                        loginName = "",
                        address = data.getString("group"),
                        nickName = data.getString("name"),
                        remark = "",
                        lableColor = ProductLableUtil.getLableColor(),
                        isTemp = 0
                    )
                    LocalDBManager(activityUI.get()!!).getTableOperation(UserOperaDao::class.java)
                        .insertUser(userBean)
                    val groupBean = GroupBean(
                        userBean.address,
                        userBean.nickName,
                        myBean!!.address,
                        data.getString("privateKey"),
                        data.getString("shadowKey"),
                        data.getString("shadowPrivate"),
                        "[]"
                    )
                    LocalDBManager(activityUI.get()!!).getTableOperation(GroupOperaDao::class.java)
                        .insertGroup(groupBean)
                }
            }
            catch (e:Exception){
                LogUtil.e("handleCreateGroup", e.toString())
            }
        }
        private fun handleMessage(text:String){
            LogUtil.e("handleMessage", text)

            try {
                val json = JSONObject(text)
                if(json.has("errCode") && json.getString("errCode") != "success"){
                    LogUtil.e("handleMessage", "respond error: $json")
                    return
                }
                when (json.getString("command")) {
                    "login" -> {
                        serviceID = json.getString("serviceID")
                        isLogin = true
                    }
                    "creategroup" -> {
                        handleCreateGroup(json)
                    }
                    "message"->{
                        val bean = Gson().fromJson(text, MsgInterItemBean::class.java)
                        recordMessage(bean)
                    }
                    "groupupdate"->{
                        val bean = Gson().fromJson(text, MsgInterItemBean::class.java)
                        updateGroup(bean)
                    }
                    "groupinfo"->{
                        val bean = Gson().fromJson(text, MsgInterItemBean::class.java)
                        groupInfo(bean)
                    }
                }
            }
            catch (e:Exception){
                LogUtil.e("handleMessage", e.toString())
            }
        }

        fun login(userBean: UserBean){
            myBean = userBean
        }
        fun sendMessage(msgBean: MsgBean, time:String, msg:String){
            val data = JSONObject()
            data.put("content", msg)
            val json = makeMessage("message", myBean!!.address,
                msgBean.peerAddress, data.toString(), time)
            sendPackage(json!!)
        }
        fun addGroup(name:String, groupID: String, privateKey:String, shadowPublic:String, shadowPrivate:String) {
            val data = JSONObject()
            data.put("group", groupID)
            data.put("name", name)
            data.put("owner", myBean!!.address)
            data.put("privateKey", privateKey)
            data.put("shadowKey", shadowPublic)
            val json = makeMessage("creategroup", myBean!!.address,
                serviceID, data.toString())
            sendPackage(json!!)

            data.put("shadowPrivate", shadowPrivate)
            commandMap.put("creategroup", data)
        }
        fun addMember(groupID:String, memberInfo: Array<JSONObject>){
            val list = JSONArray()
            for(user in memberInfo)
                list.put(user)
            val data = JSONObject()
            data.put("userList", list)
            val json = makeMessage("addmember", myBean!!.address, groupID,
                data.toString())
            sendPackage(json!!)
        }

        fun sendPackage(json:JSONObject){
            Thread(Runnable {
                synchronized(this){
                    try {
                        if (socket.isConnected) {
                            if (!json.getString("command").equals("heart"))
                                LogUtil.e("sendPackage", json.toString())
                            val length = json.toString().length
                            val data = ByteArray(length + 4)
                            data[0] = length.shr(24).toByte()
                            data[1] = length.shr(16).toByte()
                            data[2] = length.shr(8).toByte()
                            data[3] = length.and(0xff).toByte()
                            System.arraycopy(json.toString().toByteArray(), 0, data, 4, length)
                            val outputStream = socket.getOutputStream()
                            outputStream.write(data)
                        }
                    }
                    catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }).start()
        }
        fun initSocket(activity: MainActivity){
            activityUI = WeakReference(activity)
            Thread(Runnable {
                while (true) {
                    try{
                        getServerAddr()
                        val saddr = InetSocketAddress(serverAddr, serverPort)
                        LogUtil.e("initSocket", "connec to "+ serverAddr+":"+ serverPort)

                        socket = Socket()
                        //socket.soTimeout = 10000
                        try {
                            socket.connect(saddr, 5000)
                            while(true){
                                val inputStream = socket.getInputStream()
                                val head = ByteArray(4)
                                if(inputStream.read(head) == -1)
                                    break;
                                val lenght = head[0].toInt().and(0xff).shl(24) +
                                        head[1].toInt().and(0xff).shl(16) +
                                        head[2].toInt().and(0xff).shl(8) +
                                        head[3].toInt().and(0xff)
                                val data = ByteArray(lenght)
                                if(inputStream.read(data) == -1)
                                    break;
                                handleMessage(String(data))
                            }
                        }
                        catch (e:Exception){
                            LogUtil.e("initSocket", e.toString())
                        }
                        isLogin = false
                        socket.close()
                    }
                    catch (e:Exception){
                        LogUtil.e("initSocket", e.toString())
                    }
                }

            }).start()
            Thread(Runnable {
                while(true) {
                    Thread.sleep(5000)
                    if (socket.isConnected && myBean != null) {
                        val json = JSONObject()
                        json.put("command", if(isLogin){"heart"}else{"login"});
                        json.put("user", myBean?.address)
                        sendPackage(json)
                    }
                }
            }).start()

        }
        fun resetSocket(){
            LogUtil.e("resetSocket", "restart")
            socket.close()
        }
    }
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