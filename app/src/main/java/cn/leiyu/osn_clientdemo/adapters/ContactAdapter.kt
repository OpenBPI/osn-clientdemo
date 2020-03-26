package cn.leiyu.osn_clientdemo.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.ShapeDrawable
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import butterknife.BindView
import butterknife.ButterKnife
import cn.leiyu.base.activity.BaseActivity
import cn.leiyu.base.adapter.ImplBaseAdapter
import cn.leiyu.base.utils.AddressUtil
import cn.leiyu.osn_clientdemo.R
import cn.leiyu.osn_clientdemo.activity.SubBaseFragment
import cn.leiyu.osn_clientdemo.activity.home.contact.UpdateActivity
import cn.leiyu.osn_clientdemo.beans.UserBean

/**
 * 联系人适配器
 */
class ContactAdapter(context: Context, data: MutableList<UserBean>)
    : ImplBaseAdapter<UserBean>(context, data), View.OnClickListener {
    override fun getLayoutId(position: Int): Int {
        return R.layout.list_item_contact
    }

    override fun getHolder(position: Int, view: View?): BaseViewHolder? {
        var holder: ViewHolder? = null
        if(view != null){
            val tag = view.tag
            holder = tag as? ViewHolder ?: ViewHolder(view, getLayoutId(position))
        }
        return holder
    }

    override fun <B : BaseViewHolder> initView(position: Int, holder: B) {
        with(holder as ViewHolder){
            edit.setOnClickListener(this@ContactAdapter)
        }
    }

    override fun <B : BaseViewHolder> showView(position: Int, holder: B) {
        val bean = data[position]
        with(holder as ViewHolder){
            var name = if(TextUtils.isEmpty(bean.nickName))" " else bean.nickName
            head.text = name.substring(name.length - 1, name.length)
            val shape = context.resources.getDrawable(R.drawable.bg_circle) as GradientDrawable
            shape.setColor(bean.lableColor)
            head.setBackgroundDrawable(shape)
            if(AddressUtil.isGroup(bean.address))
                name = "G: " + name
            nick.text = name
            address.text = bean.address
            edit.text = String.format(edit.text.toString(), "")
            edit.contentDescription = "$position"
        }
    }

    override fun onClick(v: View?) {
        val pos = v?.contentDescription.toString().toInt()
        (context as BaseActivity).startActivityForResult(Intent(context, UpdateActivity::class.java)
            .putExtra("bean", data[pos]), 102)
    }

    class ViewHolder(view: View, resId: Int): BaseViewHolder(view, resId){
        init {
            ButterKnife.bind(this, view)
        }

        @BindView(R.id.item_head)
        lateinit var head: TextView
        @BindView(R.id.item_nick)
        lateinit var nick: TextView
        @BindView(R.id.item_friendId)
        lateinit var address: TextView
        @BindView(R.id.item_edit)
        lateinit var edit: TextView
    }
}