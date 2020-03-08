package cn.leiyu.base.weak

import android.content.ComponentCallbacks
import android.widget.AdapterView
/**
 * 该抽象类作为[匿名]内部类的调用外部对象的缓冲对象（解决内存泄漏）
 * Created by zhang on 2018/3/31.
 */
abstract class WeakOnItemClick<T :ComponentCallbacks>(context: T) : AbsWeakContext<T>(context), AdapterView.OnItemClickListener {
}