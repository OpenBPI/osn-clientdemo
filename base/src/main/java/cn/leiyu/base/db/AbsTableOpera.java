package cn.leiyu.base.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import cn.leiyu.base.utils.LogUtil;

/**
 * 该类为操作数据库表的对象，为所有表的基类<br/>
 * 如果是子类，必须提供一个{@link SQLiteDatabase}对象的有参构造；<br/>
 * 如果自己重写了构造，又要调用自己构造，建议重写{@link AbsDBManager#getTableOperation(Class<T> operation)}方法
 * <br/>created by Yao
 */
public class AbsTableOpera implements AbsDBManager.IOperaMethod {

    /**
     * 数据库不存在
     */
    public static final int DONT_DB = -99;
    /**
     * 操作错误
     */
    public static final int OPERATION_ERROR = -1;

    protected static String TAG;
    protected SQLiteDatabase mSqliteDB;
    protected String mTableName;
    public AbsTableOpera(SQLiteDatabase database){
        this.mSqliteDB = database;
        TAG = this.getClass().toString();
    }

    public AbsTableOpera setOperaTable(String tableName){
        LogUtil.e(TAG, "name ="+tableName);
        this.mTableName = tableName;
        return this;
    }

    @Override
    public long insert(String nullColumnHack, ContentValues columnDatas, int conflictAlgorithm) {
        return isDB() == DONT_DB ? DONT_DB : mSqliteDB.insertWithOnConflict(mTableName, nullColumnHack, columnDatas, conflictAlgorithm);
    }

    @Override
    public int delete(String whereClause, String[] whereArgs) {
        return isDB() == DONT_DB ? DONT_DB : mSqliteDB.delete(mTableName, whereClause, whereArgs);
    }

    @Override
    public int update(ContentValues values, String whereClause, String[] whereArgs) {
        return isDB() == DONT_DB ? DONT_DB : mSqliteDB.update(mTableName, values, whereClause, whereArgs);
    }

    @Override
    public Cursor query(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return isDB() == DONT_DB ? null : mSqliteDB.query(mTableName, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    /**
     * 根据条件 查询指定表的指定列
     * @param columns 结果集，如果为null或0，结果集为所有列
     * @param selection 查询条件
     * @param selectionArgs 查询条件值数组
     * @return 结果集数据集
     */
    public final String[][] query(String[] columns, String selection, String[] selectionArgs){
        String[][] tmp = null;
        Cursor c = query(columns, selection, selectionArgs, null, null, null);
        if(c != null){
            int count = c.getCount();
            int colNum = ((columns == null || columns.length == 0) ? c.getColumnCount() : columns.length);
            tmp = new String[count][colNum];
            for (int q = 0; q < count; q++){
                c.moveToNext();
                for(int j = 0; j < colNum; j++){
                    String str = c.getString(c.getColumnIndex(c.getColumnName(j)));
                    tmp[q][j] = str;
                }
            }
        }
        return tmp;
    }

    protected final int isDB(){
        if(mSqliteDB == null){
            LogUtil.e(TAG, "数据库不存在");
            return DONT_DB;
        }
        return 1;
    }
}
