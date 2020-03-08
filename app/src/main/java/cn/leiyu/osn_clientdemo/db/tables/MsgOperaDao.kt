package cn.leiyu.osn_clientdemo.db.tables

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import cn.leiyu.base.db.AbsTableOpera
import cn.leiyu.base.utils.LogUtil
import cn.leiyu.osn_clientdemo.Constant
import cn.leiyu.osn_clientdemo.beans.MsgBean
import cn.leiyu.osn_clientdemo.beans.UserBean

/**
 * 消息 操作处理
 */
class MsgOperaDao(db: SQLiteDatabase): AbsTableOpera(db) {

    private val pageSize = 2 * Constant.PAGE_SIZE

    init {
        mTableName = "msg"
    }

    fun update(_id: Long, state: Int = 1){
        val cv = ContentValues()
        cv.put("sendSuccess", state)
        try {
            update(cv, "_id = $_id", null)
        }catch (e: SQLException){
            e.printStackTrace()
            LogUtil.e(TAG, "消息状态修改异常 ${e.localizedMessage}")
        }
    }

    fun update(msgBean: MsgBean){
        val cv = ContentValues()
        cv.put("isRead", 0)
        try{
            update(cv, "sendId = ? and peerId = ?", arrayOf("${msgBean.peerId}", "${msgBean.sendId}"))
        }catch (e: SQLException){
            e.printStackTrace()
            LogUtil.e(TAG, "修改未读数状态异常 ${e.localizedMessage}")
        }
    }

    fun query(sendId: Long, page: Int = 1): MutableList<MsgBean>{
        val sql = """select tmp.*,nickName,address,lableColor from (
            |select peerId, msg, max(time) time, sum(isRead) isRead, _id from(
            |select peerId, msg, time, isRead, _id from msg where sendId = '$sendId' 
            |union all 
            |select sendId as peerId, msg, time, isRead, _id from msg where peerId = '$sendId') 
            |group by peerId 
            |limit ${((page - 1) * pageSize)},$pageSize) as tmp 
            |left join user on tmp.peerId = user._id order by tmp.time desc;""".trimMargin()
        val data = arrayListOf<MsgBean>()
        var c: Cursor? = null
        try{
            c =  mSqliteDB.rawQuery(sql, null)
            c?.let {
                while(c.moveToNext()){
                    //c.getInt(c.getColumnIndex("sendId"))
                    val bean = MsgBean(_id = c.getInt(c.getColumnIndex("_id")),
                        sendId = sendId.toInt(),
                        peerId = c.getInt(c.getColumnIndex("peerId")),
                        peerName = c.getString(c.getColumnIndex("nickName")),
                        peerAddress = c.getString(c.getColumnIndex("address")),
                        msg = c.getString(c.getColumnIndex("msg")),
                        time = c.getString(c.getColumnIndex("time")),
                        peerLableColor = c.getInt(c.getColumnIndex("lableColor")),
                        unRead = c.getInt(c.getColumnIndex("isRead")))
                    data.add(bean)
                }
            }
        }catch (e: SQLException){
            e.printStackTrace()
        }finally {
            c?.close()
        }
        return data
    }

    fun query(peerId: Int, user: UserBean, page: Int = 1):MutableList<MsgBean>{
        val data = arrayListOf<MsgBean>()
        var c: Cursor? = null
        try{
            c = query(null, "(sendId = ${user._id} and peerId = $peerId) or (sendId = $peerId and peerId = ${user._id})",
                null,
                null, null, "time desc limit ${((page - 1)*Constant.PAGE_SIZE)},${Constant.PAGE_SIZE}")
            c?.let {
                while (c.moveToNext()){
                    val id = c.getInt(c.getColumnIndex("sendId"))
                    val params = if(id == user._id.toInt()){
                        arrayOf(user.nickName, user.address, "${user.lableColor}")
                    }else{
                        arrayOf("", "", "0")
                    }
                    val bean = MsgBean(_id = c.getInt(c.getColumnIndex("_id")),
                        sendId = id, peerId = c.getInt(c.getColumnIndex("peerId")),
                        peerName = params[0], peerAddress = params[1], peerLableColor = params[2].toInt(),
                        msg = c.getString(c.getColumnIndex("msg")),
                        time = c.getString(c.getColumnIndex("time")),
                        unRead = 0,
                        sendSuccess = c.getInt(c.getColumnIndex("sendSuccess")))
                    data.add(bean)
                }
            }
        }catch (e: SQLException){
            e.printStackTrace()
        }finally {
            c?.close()
        }
        return data
    }
}