package cn.leiyu.base.weak

import android.content.ComponentCallbacks
import java.lang.ref.WeakReference
/**
 * 该抽象类作为[匿名]内部类的调用外部对象的缓冲对象（解决内存泄漏）
 * Created by zhang on 2018/3/22.
 */
abstract class AbsWeakContext<T : ComponentCallbacks> {

    private val weakContext:WeakReference<T>

    constructor(context :T){
        weakContext = WeakReference(context)
    }
    /**
     * 获取内部类调用的外部对象(外部为activity or fragment)，强烈建议使用
     * @return 外部对象
     */
    fun getWeakContext(): T?{
        return weakContext.get()
    }
}