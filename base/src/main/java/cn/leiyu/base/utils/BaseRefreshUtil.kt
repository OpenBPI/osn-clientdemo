package cn.leiyu.base.utils

import android.content.ComponentCallbacks
import cn.leiyu.base.weak.AbsWeakContext
import com.chanven.lib.cptr.PtrClassicFrameLayout
import com.chanven.lib.cptr.PtrDefaultHandler
import com.chanven.lib.cptr.PtrFrameLayout
import com.chanven.lib.cptr.loadmore.OnLoadMoreListener

/**
 * 刷新工具封装对象
 */
open class BaseRefreshUtil<T : ComponentCallbacks> : AbsWeakContext<T>, OnLoadMoreListener {

    private val mRefreshView: PtrClassicFrameLayout
    private val TAG :String

    constructor(context : T, view :PtrClassicFrameLayout): super(context){
        TAG = this::class.java.simpleName
        mRefreshView = view
    }

    /**
     * 初始化刷新控件信息
     */
    open fun initView(){
        mRefreshView.setOnLoadMoreListener(this)
        mRefreshView.setLastUpdateTimeRelateObject(this)
        mRefreshView.disableWhenHorizontalMove(true)
        mRefreshView.setPtrHandler(object :PtrDefaultHandler(){
            override fun onRefreshBegin(frame: PtrFrameLayout?) {
                val tmp = getWeakContext()
                if(tmp is IRefreshCallback<*>)
                    tmp.onPullDownRefresh()
            }
        })
    }

    /**
     * 加载更多回调接口
     */
    override fun loadMore() {
        val tmp = getWeakContext()
        if(tmp is IRefreshCallback<*>)tmp.onPullUpRefresh()
    }

    /**
     * 重置刷新
     */
    fun resetRefresh(page :Int){
        getWeakContext().let {
            if(page == 1){
                mRefreshView.refreshComplete()
            }else mRefreshView.loadMoreComplete(true)
        }
    }

    /**
     * 提供界面刷新回调接口
     */
    interface IRefreshCallback<T : ComponentCallbacks> {
        /**
         * 下拉刷新
         */
        fun onPullDownRefresh()

        /**
         * 上拉刷新
         */
        fun onPullUpRefresh()

        /**
         * 刷新工具
         */
        fun getRefreshUtil():BaseRefreshUtil<T>
    }
}