package cn.leiyu.base.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

abstract class ImplBaseAdapter<V> constructor(val context :Context, var data: MutableList<V>) : BaseAdapter() {

    override fun getCount(): Int {
        return if (data.isEmpty()) 0 else data.size
    }

    override fun getItem(i: Int): Any {
        return data[i]!!
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, v: View?, viewGroup: ViewGroup): View? {
        var view = v
        var holder = getHolder(i, view)
        val resId = getLayoutId(i)
        if (view == null || holder == null || holder.resId != resId) {
            view = LayoutInflater.from(context).inflate(resId, viewGroup, false)
            holder = getHolder(i, view)
            initView(i, holder!!)
            view!!.tag = holder
        }
        showView(i, holder)
        view = diffView(i, holder)
        return view
    }

    open fun addData(data: MutableList<V>, isReset: Boolean) {
        if (isReset) this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    /**
     * 初始化缓存view对象信息
     * @param position 当前位置
     * @param holder 缓存view
     * @param <B> 缓存view的基类
    </B> *
     */
    protected open fun <B : BaseViewHolder> initView(position: Int, holder: B) {}

    /**
     * 处理不同的布局显示
     * @param position 当前位置
     * @param holder 缓存view
     * @param <B> 缓存view的基类
     * @return
    </B> *
     */
    protected open fun <B : BaseViewHolder> diffView(position: Int, holder: B): View {
        return holder.view
    }

    /**
     * @param position 当前位置
     * @return 设置当前列表的布局
     */
    protected abstract fun getLayoutId(position: Int): Int

    /**
     * 获取缓存view对象
     * @param position 当前位置
     * @param view 布局view
     * @return
     */
    protected abstract fun getHolder(position: Int, view: View?): BaseViewHolder?

    /**
     * 显示布局信息
     * @param position 当前位置
     * @param holder 缓存view
     * @param <B> 缓存view的基类
    </B> *
     */
    protected abstract fun <B : BaseViewHolder> showView(position: Int, holder: B)

    open class BaseViewHolder(var view: View, var resId: Int)
}