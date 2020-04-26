package cn.leiyu.osn_clientdemo;

import android.Manifest;
import android.content.Intent;
import android.database.SQLException;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.leiyu.base.activity.BaseActivity;
import cn.leiyu.base.utils.LogUtil;
import cn.leiyu.base.utils.encoded.EcKeyUtils;
import cn.leiyu.osn_clientdemo.activity.user.CreateActivity;
import cn.leiyu.osn_clientdemo.beans.UserBean;
import cn.leiyu.osn_clientdemo.db.LocalDBManager;
import cn.leiyu.osn_clientdemo.db.tables.LoginOperaDao;
import cn.leiyu.osn_clientdemo.db.tables.UserOperaDao;
import cn.leiyu.osn_clientdemo.utils.ProductLableUtil;

import static cn.leiyu.base.utils.AddressUtilKt.KEY_END_WITH;
import static cn.leiyu.base.utils.AddressUtilKt.SHADOW_DIR;

/**
 * 登陆界面</br>
 * test1 OSN2yCG1pAnXhDpNPRDTVkd6asV8bJLyHFeMRDnYHkhAwxMZumRgotu3hVR8Z1QxWzNNRSi4TLstmaH5cwTje1QufotxTLkoAaGaeh2tHUT3w7cRjPS6XTCE25drZbTKLrJvxHHnd 81
 * test0 CFWCHAIN2yCGewFrJiWT5GVvqCG15HVV6dQMU9Mf6GZpiuJugdKBjR9NwMPS1otr9fXiZ1XUxeZgtWFTRfJyLRm5ncL6fgDgMXX6JmxhNNKv51S73hwqRxTy2NUA8CYkgXUJQqWRzWGJ6A 1-8
 * leiyu@yao  blockim
 */
public class LoginActivity extends BaseActivity {

    @BindView(R.id.login_word)
    EditText mETUser;
    @BindView(R.id.userPwd)
    EditText mETPWD;
//    @BindView(R.id.test_import)
//    TextView mTVTest;
    private LocalDBManager dbManager;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initView() {
        ButterKnife.bind(this);
//        if(BuildConfig.DEBUG){
//            mTVTest.setVisibility(View.VISIBLE);
//        }
    }

    @Override
    protected void initData() {
        isGrant(201, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA});
    }

    //@OnClick(R.id.test_import)
    @Override
    public void onClick(@Nullable View v) {
        switch (v.getId()){
            case R.id.loginBtn:
                try{
                    checkLogin(v);
                }catch (Exception e){
                    e.printStackTrace();
                    v.setEnabled(true);
                }
                break;
//            case R.id.test_import:
//                readFile();
//                break;
            case R.id.registerBtn:
                startActivity(new Intent(this, CreateActivity.class));
                break;
        }
    }

    @Override
    public void permissionGrant(int requestCode, @NotNull String[] permission) {
        super.permissionGrant(requestCode, permission);
        dbManager = new LocalDBManager(this);
        //判断是否首次使用
        boolean tmp  = getSharedPreferences(Constant.configFileName, MODE_PRIVATE).getBoolean(Constant.isFirstUse, true);
        if(!tmp){
            //进入主界面
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }else{
            Constant.API.SERVICE_HOST = getString(R.string.initUrl);
            getSharedPreferences(Constant.configFileName, MODE_PRIVATE)
                    .edit().putString(Constant.SERVICE_API, Constant.API.SERVICE_HOST).apply();
        }
    }

    private void checkLogin(View v){
        String name = mETUser.getText().toString().trim();
        if(!TextUtils.isEmpty(name)){
            List<UserBean> users = dbManager.getTableOperation(UserOperaDao.class).queryUser(name);
            if(users.isEmpty())name = getString(R.string.userError);
            else{
                String pwd = mETPWD.getText().toString().trim();
                if(TextUtils.isEmpty(pwd)){
                    showToast(getString(R.string.inputPwd));
                    return;
                }
                name = users.get(0).getAddress();
                v.setEnabled(false);
                try {
                    String key = EcKeyUtils.getPrivateKey(pwd,  getFilesDir()+ SHADOW_DIR,  name + KEY_END_WITH);
                    if(TextUtils.isEmpty(key) || "error".equalsIgnoreCase(key)){
                        v.setEnabled(true);
                        showToast(getString(R.string.pwdError));
                        return;
                    }
                    //保持登陆状态
                    getSharedPreferences(Constant.configFileName, MODE_PRIVATE)
                        .edit()
                        .putBoolean(Constant.isFirstUse, false)
                        .putString(Constant.CURRENT_ACCOUNT, new Gson().toJson(users.get(0)))
                        .apply();
                    //进入主页
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                    v.setEnabled(true);
                    LogUtil.e(getTAG(), "登陆异常 "+e.getLocalizedMessage());
                }
                return;
            }
        }else name = getString(R.string.inputUName);
        showToast(name);
    }

    private void readFile(){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/shadow";
        File file = new File(path);
        if(file.exists() && file.isDirectory()){
            File shadow = new File(getFilesDir()+"/shadow");
            if(!shadow.exists())shadow.mkdirs();
            File[] tmp = file.listFiles();
            if(tmp == null || tmp.length == 0)return;
            for(int i = 0; i< tmp.length; i++){
                File f = tmp[i];
                OutputStream os = null;
                InputStream is = null;
                byte[] bytes = new byte[512];
                try {
                    os = new FileOutputStream(shadow.getAbsolutePath()+"/"+f.getName());
                    is = new FileInputStream(f);
                    int index = -1;
                    while((index = is.read(bytes)) != -1){
                        os.write(bytes, 0 , index);
                    }
                    os.flush();

                    //写库
                    Integer[] colors = new Integer[]{getResources().getColor(R.color.bg_1484ED)};
                    UserBean bean = new UserBean(-1,"test"+i, -1,"test"+i,
                            f.getName().substring(0, f.getName().length() - 5), "",0,
                            "", ProductLableUtil.getLableColor(colors));
                    long id = dbManager.getTableOperation(LoginOperaDao.class).insert(bean);
                    if(id > 0){
                        bean.setLoginId(id);
                        dbManager.getTableOperation(UserOperaDao.class).insertUser(bean);
                    }
                } catch (FileNotFoundException | SQLException e) {
                    e.printStackTrace();
                } catch (IOException e){
                    e.printStackTrace();
                } finally {
                    try {
                        if(is != null){
                            is.close();
                        }
                        if(os != null){
                            os.close();
                        }
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
