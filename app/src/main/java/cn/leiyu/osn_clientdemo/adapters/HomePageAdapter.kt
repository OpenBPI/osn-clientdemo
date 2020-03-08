package cn.leiyu.osn_clientdemo.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter


/**
 * 主界面 底部菜单栏 导航切换
 */
class HomePageAdapter constructor(private val data: List<Fragment>, fm : FragmentManager)
    : FragmentPagerAdapter(fm, BEHAVIOR_SET_USER_VISIBLE_HINT) {

    override fun getItem(position: Int): Fragment {
        return data[position]
    }

    override fun getCount(): Int {
        return data.size
    }
}