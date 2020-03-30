package cn.leiyu.osn_clientdemo.db.tables

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import cn.leiyu.base.db.AbsTableOpera
import cn.leiyu.base.utils.LogUtil
import cn.leiyu.osn_clientdemo.Constant
import cn.leiyu.osn_clientdemo.beans.LoginBean
import cn.leiyu.osn_clientdemo.beans.UserBean

/**
 * 用户表操作对象
 */
class UserOperaDao constructor(db: SQLiteDatabase) : AbsTableOpera(db) {

    init {
        mTableName = "user"
    }

    fun insertUser(user: UserBean){
        val values = ContentValues()
        values.put("nickName", user.nickName)
        values.put("address", user.address)
        values.put("lableColor", user.lableColor)
        values.put("loginId", user.loginId)
        val id = insert(null, values, SQLiteDatabase.CONFLICT_ABORT)
        LogUtil.e(TAG, "插入成功 id=$id")
    }

    fun update(user: UserBean): Any{
        val cv = ContentValues(2)
        cv.put("nickName", user.nickName)
        cv.put("remark", user.remark)
        val hint = try{
            update(cv, "_id = ?", arrayOf("${user._id}"))
        }catch (e: SQLException){
            e.printStackTrace()
            "异常 ${e.localizedMessage}"
        }
        LogUtil.e(TAG, "修改联系人 $hint")
        return hint
    }

    fun update(userName: String="", sign: String?="",loginId: Long, address: String){
        val cv = ContentValues()
        if(userName == "")
        else cv.put("nickName", userName)
        if(sign == null)
        else cv.put("sign", sign)
        LogUtil.e(TAG, "修改用户信息"+update(cv, "address = ? and loginId = ?", arrayOf(address, "$loginId")))
    }

    fun queryUser(userName: String= ""): MutableList<UserBean>{
        val data = ArrayList<UserBean>()
        var where = ""
        if(!TextUtils.isEmpty(userName))where = " where a.userName = '$userName'"
        val c = mSqliteDB.rawQuery("select a._id loginId, a.userName, b.* from login a " +
                "left join $mTableName b on a.address = b.address $where", null)
        c?.let {
            while (c.moveToNext()){
                val bean = UserBean(loginId = c.getLong(c.getColumnIndex("loginId")),
                    loginName = c.getString(c.getColumnIndex("userName")),
                    _id = c.getLong(c.getColumnIndex("_id")),
                    nickName = c.getString(c.getColumnIndex("nickName")),
                    sign = c.getString(c.getColumnIndex("sign")),
                    remark = c.getString(c.getColumnIndex("remark")),
                    address = c.getString(c.getColumnIndex("address")),
                    lableColor = c.getInt(c.getColumnIndex("lableColor")))
                data.add(bean)
            }
            it.close()
        }
        return data
    }

    fun query(login: LoginBean, page: Int = 1): MutableList<UserBean>{
        val data = arrayListOf<UserBean>()
        val sql = "select * from "+mTableName+" where loginId = ? " +
                "limit ${(page - 1) * Constant.PAGE_SIZE},${Constant.PAGE_SIZE}"
        var c: Cursor? = null
        try{
            c = mSqliteDB.rawQuery(sql, arrayOf("${login.loginId}"))
            c?.let {
                while(c.moveToNext()){
                    val bean = UserBean(loginId = -1, loginName = "", _id = c.getLong(c.getColumnIndex("_id")),
                        nickName = c.getString(c.getColumnIndex("nickName")),
                        sign = c.getString(c.getColumnIndex("sign")),
                        remark = c.getString(c.getColumnIndex("remark")),
                        address = c.getString(c.getColumnIndex("address")),
                        lableColor = c.getInt(c.getColumnIndex("lableColor")))
                    if(bean.address == login.address && bean.nickName == "")bean.nickName = login.loginName
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

    fun queryAll(login: LoginBean): MutableList<UserBean>{
        val data = arrayListOf<UserBean>()
        val sql = "select * from "+mTableName+" where loginId = ? "
        var c: Cursor? = null
        try{
            c = mSqliteDB.rawQuery(sql, arrayOf("${login.loginId}"))
            c?.let {
                while(c.moveToNext()){
                    val bean = UserBean(loginId = -1, loginName = "", _id = c.getLong(c.getColumnIndex("_id")),
                        nickName = c.getString(c.getColumnIndex("nickName")),
                        sign = c.getString(c.getColumnIndex("sign")),
                        remark = c.getString(c.getColumnIndex("remark")),
                        address = c.getString(c.getColumnIndex("address")),
                        lableColor = c.getInt(c.getColumnIndex("lableColor")))
                    if(bean.address == login.address && bean.nickName == "")bean.nickName = login.loginName
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