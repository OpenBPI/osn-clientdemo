package cn.leiyu.base.http

import android.content.Context
import android.util.Log
import cn.leiyu.base.App
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonRequest
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

object JsonVolleyUtil {

    fun request(context: Context,
                url: String,
                tag: String,
                jsonBody: String,
                volleyListenerInterface: VolleyListenerInterface,
                timeOutDefaultFlg: Boolean,
                method: Int = Request.Method.POST){
        Log.e("request", jsonBody.toString());
        val json = object : JsonRequest<String>(method, url, jsonBody, volleyListenerInterface.responseListener(), volleyListenerInterface.errorListener()) {
            override fun parseNetworkResponse(response: NetworkResponse?): Response<String> {
                val parsed = try {
                    String(response!!.data, Charset.forName(HttpHeaderParser.parseCharset(response.headers)))
                } catch (e: UnsupportedEncodingException) {
                    // Since minSdkVersion = 8, we can't call
                    // new String(response.data, Charset.defaultCharset())
                    // So suppress the warning instead.
                    String(response!!.data)
                }
                return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response))
            }
        }
        // 为当前请求添加标记
        json.tag = tag
        // 默认超时时间以及重连次数
        val myTimeOut = if(timeOutDefaultFlg) DefaultRetryPolicy.DEFAULT_TIMEOUT_MS else VolleyRequestUtil.sTimeOut
        json.retryPolicy = DefaultRetryPolicy(myTimeOut,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        // 将当前请求添加到请求队列中
        App.queue.add(json)
    }
}