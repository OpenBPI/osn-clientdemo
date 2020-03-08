package cn.leiyu.osn_clientdemo.adapters

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import cn.leiyu.base.adapter.ImplBaseAdapter
import cn.leiyu.osn_clientdemo.R
import cn.leiyu.osn_clientdemo.beans.MsgBean
import cn.leiyu.osn_clientdemo.utils.ProductLableUtil
import java.util.*

/**
 * 发送消息适配器
 */
class SendMsgAdapter(context: Context,
                     data: MutableList<MsgBean>,
                     private val myMsgBean: MsgBean)
    : ImplBaseAdapter<MsgBean>(context, data) {
    /**
     * 上次时间
     */
    private var lastDate: String = ""
    override fun getLayoutId(position: Int): Int {
        return if(data[data.lastIndex - position].sendId == myMsgBean.sendId)R.layout.list_item_rightmsg
        else R.layout.list_item_leftmsg
    }

    /**
     * 重新追加方式
     * @param isReset true 表示倒叙添加 反正正向添加
     */
    override fun addData(data: MutableList<MsgBean>, isReset: Boolean) {
        if (isReset)this.data.addAll(0, data)
        else {
            this.data.clear()
            this.data.addAll(data)
        }
        notifyDataSetChanged()
    }

    override fun getHolder(position: Int, view: View?): BaseViewHolder? {
        var holder: ViewHolder? = null
        if(view != null){
            val tag = view.tag
            holder = tag as? ViewHolder ?: ViewHolder(view, getLayoutId(position))
        }
        return holder
    }

    override fun <B : BaseViewHolder> showView(position: Int, holder: B) {
        val bean = data[data.lastIndex - position]
        with(holder as ViewHolder){
            val currentTime = bean.time.toLong()*1000
//            val lastTime = if((data.lastIndex - position + 1) <= data.lastIndex)
//                data[data.lastIndex - position + 1].time.toLong() * 1000
//            else currentTime
            val current = ProductLableUtil.showTime(currentTime, Date(), false)
            time.text = if(lastDate == current && position > 0)"" else{
                lastDate = current
                current
            }
            val shap = context.resources.getDrawable(R.drawable.bg_circle) as GradientDrawable
            nick.text = if(myMsgBean.sendId == bean.sendId){
                shap.setColor(bean.peerLableColor)
                getName(bean.peerName)
            } else{
                shap.setColor(myMsgBean.peerLableColor)
                getName(myMsgBean.peerName)
            }
            nick.setBackgroundDrawable(shap)
            msg.text = bean.msg
        }
    }

    private fun getName(str: String?): String{
        return str?.substring(str.length - 1, str.length)!!
    }

    class ViewHolder(v: View, resId: Int): BaseViewHolder(v, resId){
        init {
            ButterKnife.bind(this, v)
        }
        @BindView(R.id.item_time)
        lateinit var time: TextView
        @BindView(R.id.item_nick)
        lateinit var nick: TextView
        @BindView(R.id.item_msg)
        lateinit var msg: TextView
    }
}