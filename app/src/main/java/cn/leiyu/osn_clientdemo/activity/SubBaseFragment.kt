package cn.leiyu.osn_clientdemo.activity

import butterknife.ButterKnife
import butterknife.Unbinder
import cn.leiyu.base.activity.fragment.BaseFragment

/**
 * 实现BaseFragment的子类，实现扩展
 */
abstract class SubBaseFragment : BaseFragment() {

    private var unBinder: Unbinder? = null

    override fun initView() {
        fragmentView?.let {
            unBinder = ButterKnife.bind(this, it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unBinder?.unbind()
    }


}