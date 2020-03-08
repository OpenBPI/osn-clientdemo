package cn.leiyu.osn_clientdemo.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import cn.leiyu.base.db.AbsDBManager
import cn.leiyu.osn_clientdemo.Constant

/**
 * 本地数据库管理对象
 */
class LocalDBManager constructor(context: Context)
    : AbsDBManager(context, Constant.DB_NAME, Constant.DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        var sql = "create table if not exists login(_id Integer primary key autoincrement," +
                "userName varchar(35), address text unique not null);"
        db?.execSQL(sql)
        sql = "create table if not exists user(_id Integer PRIMARY KEY autoincrement," +
                "nickName varchar(20), address text, sign text, remark text," +
                "lableColor Int, loginId int);"
        db?.execSQL(sql)
        sql = "create table if not exists msg(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "sendId Int, peerId Int, msg text, time varchar(25), isRead varchar(1)," +
                "sendSuccess varchar(1));"
        db?.execSQL(sql)
        db?.execSQL("create unique index user_unique_address on user(address, loginId)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

}