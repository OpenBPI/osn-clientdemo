package cn.leiyu.osn_clientdemo.db.tables

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import cn.leiyu.base.db.AbsTableOpera
import cn.leiyu.osn_clientdemo.beans.LoginBean

class LoginOperaDao(db: SQLiteDatabase): AbsTableOpera(db) {
    init {
        mTableName = "login"
    }

    /**
     * 注册登陆信息
     */
    fun insert(params: LoginBean): Long{
        val cv = ContentValues()
        cv.put("userName", params.loginName)
        cv.put("address", params.address)
        return insert(null, cv, SQLiteDatabase.CONFLICT_ABORT)
    }

    fun queryUser(userName: String= ""): MutableList<LoginBean>{
        val data = ArrayList<LoginBean>()
        var where = ""
        if(!TextUtils.isEmpty(userName))where = "userName = '$userName'"
        val c = query(null, where, null, null, null, null)
        c?.let {
            while (c.moveToNext()){
                val bean = LoginBean(loginId = c.getLong(c.getColumnIndex("_id")),
                    loginName = c.getString(c.getColumnIndex("userName")),
                    address = c.getString(c.getColumnIndex("address")))
                data.add(bean)
            }
            it.close()
        }
        return data
    }
}