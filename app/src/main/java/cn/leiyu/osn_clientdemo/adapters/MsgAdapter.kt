package cn.leiyu.osn_clientdemo.adapters

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import butterknife.BindView
import butterknife.ButterKnife
import cn.leiyu.base.adapter.ImplBaseAdapter
import cn.leiyu.base.utils.AddressUtil
import cn.leiyu.osn_clientdemo.Constant
import cn.leiyu.osn_clientdemo.R
import cn.leiyu.osn_clientdemo.beans.MsgBean
import cn.leiyu.osn_clientdemo.utils.ProductLableUtil
import java.util.*

/**
 * 消息适配器
 */
class MsgAdapter(context: Context, data: MutableList<MsgBean>)
    : ImplBaseAdapter<MsgBean>(context, data) {
    private val shared: SharedPreferences
    private val draft_msg: String
    init{
        shared = context.getSharedPreferences(Constant.configFileName, Context.MODE_PRIVATE)
        draft_msg = context.getString(R.string.draft_hint)
    }

    override fun getLayoutId(position: Int): Int {
        return R.layout.list_item_msg
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
        val bean = data[position]
        with(holder as ViewHolder){
            var name = bean.peerName
            head.text = name.substring(name.length - 1, name.length)
            val shape = context.resources.getDrawable(R.drawable.bg_circle) as GradientDrawable
            shape.setColor(bean.peerLableColor)
            head.setBackgroundDrawable(shape)
            if(AddressUtil.isGroup(bean.peerAddress))
                name = "G: " + name
            nick.text = name
            time.text = ProductLableUtil.showTime(bean.time.toLong() * 1000, Date())
            val tmpMsg = shared.getString("${Constant.SUBFIX_DRAFT}${bean.peerId}", "")
            msg.text = if(tmpMsg == "") bean.msg
            else HtmlCompat.fromHtml(String.format(draft_msg, tmpMsg), HtmlCompat.FROM_HTML_MODE_LEGACY)
            unRead.text = if(bean.unRead > 0){
                unRead.visibility = View.VISIBLE
                "${bean.unRead}"
            } else {
                unRead.visibility = View.INVISIBLE
                ""
            }
        }
    }

    class ViewHolder(view: View, resId: Int): BaseViewHolder(view, resId){
        init {
            ButterKnife.bind(this, view)
        }

        @BindView(R.id.unread)
        lateinit var unRead: TextView
        @BindView(R.id.item_head)
        lateinit var head: TextView
        @BindView(R.id.item_nick)
        lateinit var nick: TextView
        @BindView(R.id.item_time)
        lateinit var time: TextView
        @BindView(R.id.item_msg)
        lateinit var msg: TextView
    }
}