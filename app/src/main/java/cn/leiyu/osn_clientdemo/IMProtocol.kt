package cn.leiyu.osn_clientdemo

import android.content.Context
import android.os.Handler
import android.os.Message
import cn.leiyu.base.http.JsonVolleyUtil
import cn.leiyu.base.http.VolleyListenerInterface
import cn.leiyu.base.utils.LogUtil
import cn.leiyu.base.utils.encoded.EcKeyUtils
import cn.leiyu.osn_clientdemo.activity.home.HomeFragment
import cn.leiyu.osn_clientdemo.beans.UserBean
import cn.leiyu.osn_clientdemo.utils.Base58
import com.android.volley.VolleyError
import org.json.JSONObject

class IMProtocol {
    companion object{
        var serviceID: String = ""

        fun setServiceOSNID(jsonResult: String){
            try{
                val json = JSONObject(jsonResult)
                serviceID = json.getString("serviceID")
            }
            catch (e: Exception){
                LogUtil.e("getServiceOSNID", e.message)
            }
        }
        fun getMessage(what:Int, msg:String):Message{
            val msg = Message()
            msg.what = what
            msg.obj = msg
            return msg
        }
        fun addGroup(context: Context, handler: Handler, bean: UserBean, groupInfo: Array<String>) {
            val groupID = groupInfo[0]
            val groupPrivateKey = groupInfo[1]
            val gsShadowPubKey = groupInfo[2]
            val gsShadowPriKey = groupInfo[3]
            val data = """{"groupID":"${groupID}", "privaKey":"${groupPrivateKey}", "shadowKey":"${gsShadowPubKey}"}"""
            val encData = EcKeyUtils.ECEncrypt(serviceID, data.toByteArray())
            val privateKey = EcKeyUtils.getEcPrivateKeyFromHex("83c9efe453ffaf38dc61cd336f72be1bfe4a599ae8d889e4c8a005f9bf5f1190")
            LogUtil.e("test", privateKey.toString())
            val ddd = EcKeyUtils.ECDecrypt(privateKey, encData)
            val signData = EcKeyUtils.signReturnBase58(HomeFragment.gPrivateKey, encData.toByteArray())
            val signFlag = EcKeyUtils.verifySign(encData.toByteArray(), bean.address, Base58.decode(signData))
            val json = """{"command":"creategroup", "owner":"${bean.address}", "data":"${encData}", "sign":"${signData}"}"""
            JsonVolleyUtil.request(context,
                Constant.API.SERVICE_HOST,
                "creategroup", json,
                object : VolleyListenerInterface(context, mListener, mErrorListener) {
                    override fun onMySuccess(result: String?) {
                        LogUtil.e("SendComplete", result)
                        handler.handleMessage(getMessage(0, "true"))
                    }

                    override fun onMyError(error: VolleyError?) {
                        LogUtil.e("SendComplete", "error = $error")
                        handler.handleMessage(getMessage(0, "false"))
                    }
                }, false
            )
        }
    }
}