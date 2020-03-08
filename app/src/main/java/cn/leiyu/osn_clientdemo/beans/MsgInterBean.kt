package cn.leiyu.osn_clientdemo.beans

/**
 * 服务器-消息接口对象
 */
data class MsgInterBean(val errCode: Int = -1,
                        val data: MutableList<MsgInterItemBean>)

/**
 * 服务器-消息对象
 */
data class MsgInterItemBean(val content: String ="",
                            val from: String,
                            val to: String,
                            val crypto: String,
                            val description: String,
                            val hash: String,
                            val sign: String,
                            val timestamp: String)