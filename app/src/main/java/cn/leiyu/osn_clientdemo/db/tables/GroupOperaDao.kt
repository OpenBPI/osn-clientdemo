package cn.leiyu.osn_clientdemo.db.tables

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import cn.leiyu.base.db.AbsTableOpera
import cn.leiyu.base.utils.LogUtil
import cn.leiyu.osn_clientdemo.beans.GroupBean

class GroupOperaDao constructor(db: SQLiteDatabase) : AbsTableOpera(db) {

    init {
        mTableName = "groups"
    }

    fun insertGroup(group: GroupBean){
        val values = ContentValues()
        values.put("osnid", group.osnid)
        values.put("name", group.name)
        values.put("owner", group.owner)
        values.put("privateKey", group.privateKey)
        values.put("publicShadow", group.publicShadow)
        values.put("privateShadow", group.privateShadow)
        values.put("userList", group.userList)
        val id = insert(null, values, SQLiteDatabase.CONFLICT_ABORT)
        LogUtil.e(TAG, "插入成功 id=$id")
    }

    fun updateUserList(groupID:String, userList:String): Any{
        val cv = ContentValues(1)
        cv.put("userList", userList)
        val hint = try{
            update(cv, "osnid = ?", arrayOf(groupID))
        }catch (e: SQLException){
            e.printStackTrace()
            "异常 ${e.localizedMessage}"
        }
        LogUtil.e(TAG, "修改联系人 $hint")
        return hint
    }

    @SuppressLint("Recycle")
    fun query(osnid: String= ""): GroupBean?{
        var bean: GroupBean? = null
        val c = mSqliteDB.rawQuery("select * from groups where osnid = '${osnid}'", null)
        c?.let {
            while (c.moveToNext()){
                c.getColumnIndex("")
                bean = GroupBean(c.getString(c.getColumnIndex("osnid")),
                    c.getString(c.getColumnIndex("name")),
                    c.getString(c.getColumnIndex("owner")),
                    c.getString(c.getColumnIndex("privateKey")),
                    c.getString(c.getColumnIndex("publicShadow")),
                    c.getString(c.getColumnIndex("privateShadow")),
                    c.getString(c.getColumnIndex("userList")))
            }
            it.close()
        }
        return bean
    }
}