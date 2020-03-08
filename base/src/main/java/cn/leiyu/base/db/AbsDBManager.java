package cn.leiyu.base.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import cn.leiyu.base.BuildConfig;
import cn.leiyu.base.utils.LogUtil;
import cn.leiyu.base.utils.PrintWriteUtils;

/**
 * 数据库操作抽象类
 * created by Yao
 */
public abstract class AbsDBManager {
    protected static String TAG = "";
    /**
     * 当前上下文
     */
    protected Context mContext;
    /**
     * 数据库对象
     */
    protected SQLiteDatabase mSqliteDB;

    public AbsDBManager(Context context, String dbName, int version){
        this(context, new File(dbName), version);
    }

    public AbsDBManager(Context context, File file, int version){
        if(context == null){
            throw new IllegalArgumentException("context");
        }else if(file == null){
            throw new IllegalArgumentException("file dont null");
        }else if(file.isDirectory()){
            throw new IllegalArgumentException("file is directory");
        }
        String dbName = file.getName();
        if(TextUtils.isEmpty(dbName)){
            LogUtil.e(this.getClass().toString(), "---- database name dont null");
            return;
        }
        TAG = this.getClass().toString();
        String dbPath = file.getParentFile() == null ? "" : file.getParentFile().getAbsolutePath();
        if(TextUtils.isEmpty(dbPath) || file.getPath().startsWith(".")){
            LogUtil.e(TAG, "创建数据库在当前应用程序目录");
            mSqliteDB = context.openOrCreateDatabase(dbName, 0, null);
        }else{
            if(!file.getParentFile().exists()){
                LogUtil.e(TAG, "创建数据库在指定目录");
                file.getParentFile().mkdirs();
            }
            mSqliteDB = SQLiteDatabase.openOrCreateDatabase(file.getAbsolutePath(), null);
        }
        initTable(version);
    }

    private void initTable(int version){
        if(mSqliteDB != null && mSqliteDB.isOpen()){
            if(mSqliteDB.getVersion() == 0){
                mSqliteDB.beginTransaction();
                try {
                    onCreate(mSqliteDB);
                    mSqliteDB.setVersion(version);
                    mSqliteDB.setTransactionSuccessful();
                }finally {
                    mSqliteDB.endTransaction();
                }
            }
            else{
                if(mSqliteDB.needUpgrade(version)){
                    mSqliteDB.beginTransaction();
                    try{
                        onUpgrade(mSqliteDB, mSqliteDB.getVersion(), version);
                        mSqliteDB.setVersion(version);
                        mSqliteDB.setTransactionSuccessful();
                    }finally {
                        mSqliteDB.endTransaction();
                    }
                }
            }
        }else{
            LogUtil.e(TAG, "database is null or dont open");
        }
    }

    /**
     * 获取具体操作表的对象
     * @param operation 数据处理对象
     * @param <T>
     * @return
     */
    public <T extends IOperaMethod> T getTableOperation(Class<T> operation) {
        try{
            Constructor<T> tmp = operation.getConstructor(SQLiteDatabase.class);
            return tmp.newInstance(mSqliteDB);
        }catch (NoSuchMethodException e) {
            e.printStackTrace();
            if(!BuildConfig.DEBUG)PrintWriteUtils.writeErrorLog(false, TAG+"没有该方法 "+e.getLocalizedMessage());
        }catch (IllegalAccessException e){
            e.printStackTrace();
            if(!BuildConfig.DEBUG)PrintWriteUtils.writeErrorLog(false, TAG+"实例化对象时的参数错误 "+e.getLocalizedMessage());
        }catch (InvocationTargetException e) {
            e.printStackTrace();
            if(!BuildConfig.DEBUG)PrintWriteUtils.writeErrorLog(false, TAG+"实例化对象调用错误 "+e.getLocalizedMessage());
        }catch (InstantiationException e){
            e.printStackTrace();
            if(!BuildConfig.DEBUG)PrintWriteUtils.writeErrorLog(false, TAG+"实例化对象错误 "+e.getLocalizedMessage());
        }catch (Exception e){
            if(!BuildConfig.DEBUG)PrintWriteUtils.writeErrorLog(false, TAG+"错误 "+e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * 关闭数据库
     */
    public void close(){
        if(mSqliteDB != null && mSqliteDB.isOpen() && !mSqliteDB.isDbLockedByCurrentThread()){
            mSqliteDB.close();
            mSqliteDB = null;
        }
    }

    /**
     * 创建数据库表
     * @param db
     */
    protected abstract void onCreate(SQLiteDatabase db);

    /**
     * 升级数据库表
     * @param db
     * @param oldVersion 旧版本
     * @param newVersion 新版本
     */
    protected abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    /**
     * sqlite数据库表操作接口
     * created by Yao on 2018-06-14
     */
    public interface IOperaMethod{
        /**
         * 新增
         * @param nullColumnHack 空列
         * @param columnDatas 列数据
         * @param conflictAlgorithm 冲突算法
         * @return 新增条数
         */
        long insert(String nullColumnHack, ContentValues columnDatas, int conflictAlgorithm);

        /**
         * 删除数据
         * @param whereClause 删除条件
         * @param whereArgs 删除条件值
         * @return 删除数量
         */
        int delete(String whereClause, String[] whereArgs);

        /**
         * 修改数据
         * @param values 修改列数据
         * @param whereClause 修改条件
         * @param whereArgs 修改条件值
         * @return
         */
        int update(ContentValues values, String whereClause, String[] whereArgs);

        /**
         * 查询数据
         * @param columns 筛选列
         * @param selection 筛选条件
         * @param selectionArgs 筛选条件值
         * @param groupBy 分组
         * @param having 根据分组条件
         * @param orderBy 排序
         * @return
         */
        Cursor query(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy);
    }
}
