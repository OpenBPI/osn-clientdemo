package cn.leiyu.osn_clientdemo.activity

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import cn.leiyu.osn_clientdemo.R

/**
 * 实现BaseActivity的子类，实现扩展
 */
abstract class SubBaseActivity :AbsParentBaseActivity() {

    private var unBinder: Unbinder? = null
    /**
     * 顶部导航父布局
     */
    @BindView(R.id.base_top_toolbar)
    protected lateinit var topNav : LinearLayout
    @BindView(R.id.toolbar)
    protected lateinit var toolBar: Toolbar
    /**
     * 标题
     */
    @BindView(R.id.toolbar_title)
    protected lateinit var topTitle: TextView
    /**
     * 菜单
     */
    @BindView(R.id.toolbar_menu)
    protected lateinit var topMenu: TextView
    /**
     * 下滑线
     */
    @BindView(R.id.top_line)
    protected lateinit var topLine: View

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        unBinder = ButterKnife.bind(this)
        toolBar.setNavigationOnClickListener{
            it.id = R.id.iv_back
            onClick(it)
        }
    }

    @OnClick(R.id.toolbar_menu, R.id.base_top_toolbar)
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.iv_back-> onBackPressed()
            else -> super.onClick(v)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unBinder?.unbind()
    }

    /**
     * 设置顶部导航背景
     */
    protected fun setTopNavBackground(navColorId: Int=resources.getColor(R.color.white),
                                      lineColorId: Int = resources.getColor(R.color.white)){
        toolBar.setBackgroundColor(navColorId)
        topLine.setBackgroundColor(lineColorId)
    }
}