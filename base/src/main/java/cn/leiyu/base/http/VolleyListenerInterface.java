package cn.leiyu.base.http;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.lang.ref.WeakReference;

import cn.leiyu.base.App;
import cn.leiyu.base.utils.LogUtil;

public abstract class VolleyListenerInterface {
    public WeakReference<Context> mContext;
    public static Response.Listener<String> mListener;
    public static Response.ErrorListener mErrorListener;
    protected long localMsgId;

    public VolleyListenerInterface(Context context, Response.Listener<String> listener,
                                   Response.ErrorListener errorListener) {
        this.mContext = new WeakReference<>(context);
        this.mErrorListener = errorListener;
        this.mListener = listener;
    }

    public VolleyListenerInterface(Context context, long localMsgId, Response.Listener<String> listener,
                                   Response.ErrorListener errorListener){
        this(context, listener, errorListener);
        this.localMsgId = localMsgId;
    }

    // 请求成功时的回调函数
    public abstract void onMySuccess(String result);

    // 请求失败时的回调函数
    public abstract void onMyError(VolleyError error);

    // 创建请求的事件监听
    public Response.Listener<String> responseListener() {
        mListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Log.e("response", s);
                onMySuccess(s);
            }
        };
        return mListener;
    }

    // 创建请求失败的事件监听
    public Response.ErrorListener errorListener() {
        mErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                onMyError(volleyError);
                Context tmp = mContext.get();
                LogUtil.e((tmp == null ? "VolleyListenerInterface" : tmp.getClass().toString()), "访问错误 "+volleyError.getMessage());
                Toast.makeText( tmp != null ? tmp : App.getAppContext(),
                        volleyError.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        return mErrorListener;
    }
}

