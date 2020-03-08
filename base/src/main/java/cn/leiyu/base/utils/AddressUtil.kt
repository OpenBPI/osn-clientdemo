@file:JvmMultifileClass
package cn.leiyu.base.utils

import cn.leiyu.base.utils.encoded.EcKeyUtils
import org.bitcoinj.crypto.ChildNumber
import java.lang.Thread.sleep

/**
 * 身份目录
 */
const val EC_DIR = "/identify/"
/**
 * 钱包目录
 */
const val WALLET_DIR = "/wallet/"
/**
 * shadow目录
 */
const val SHADOW_DIR = "/shadow/"
/**
 * 后缀名
 */
const val KEY_END_WITH = ".prik"
/**
 * 助记词后缀
 */
const val WORD_END_WITH = ".mcw"
const val KEY_FILE_NAME = "mainKey"

class AddressUtil constructor(private val filesDir: String) {

    var address: Array<String?> = arrayOfNulls(2)

    /**
     * 生成助记词
     */
    fun createWord(): String = EcKeyUtils.createMnemonic()

    fun getPrivateKey(pwd: String, address: String): String{
        return EcKeyUtils.getPrivateKey(pwd, "${this.filesDir}$EC_DIR", "$address$KEY_END_WITH")
    }
    fun genIdentify(mnemonicStr: String, pwd: String): String?{
        val params = createPub(mnemonicStr)
        address[0] = params[3]
        // 保存私钥 address.prik
        if (!EcKeyUtils.savePrivateKey(params[1], null,  "${this.filesDir}$EC_DIR", "${params[3]}$KEY_END_WITH")) {
            return null
        }
        if (!EcKeyUtils.savePrivateKey(params[1], null,  "${this.filesDir}$EC_DIR", "${KEY_FILE_NAME}$KEY_END_WITH")) {
            return null
        }
        if (!EcKeyUtils.savePrivateKey(params[2], pwd, "${this.filesDir}$SHADOW_DIR", "${params[3]}$KEY_END_WITH")) {
            return null
        }
        // 存储助记词   address.mcw
        if (!EcKeyUtils.saveMnemonic(pwd, mnemonicStr, "${this.filesDir}$EC_DIR", "${params[3]}$WORD_END_WITH")) {
            return null
        }
        return params[0]
    }

    fun genWallet(mnemonicStr: String, pwd: String): String?{
        val params = createPub(mnemonicStr)
        address[1] = params[3]
        // 保存私钥 address.prik
        if (!EcKeyUtils.savePrivateKey(params[1], null,  "${this.filesDir}$EC_DIR", "${params[3]}$KEY_END_WITH")) {
            return null
        }
        if (!EcKeyUtils.savePrivateKey(params[2], pwd, "${this.filesDir}$SHADOW_DIR", "${params[3]}$KEY_END_WITH")) {
            return null
        }
        // 存储助记词   address.mcw
        if (!EcKeyUtils.saveMnemonic(pwd, mnemonicStr, "${this.filesDir}$EC_DIR", "${params[3]}$WORD_END_WITH")) {
            return null
        }
//        // 保存私钥 address.prik
//        if (!EcKeyUtils.savePrivateKey(params[1], pwd, "$filesDir$WALLET_DIR",  "${params[2]}$KEY_END_WITH")) {
//            return null
//        }
//        // 存储助记词   address.mcw
//        if (!EcKeyUtils.saveMnemonic(pwd, params[0], "$filesDir$WALLET_DIR", "${params[2]}$WORD_END_WITH")) {
//            return null
//        }
        return params[0]
    }

    private fun createPub(mnemonicStr: String): Array<String>{
        // 使用master衍生出keypair 用于通信 登陆  m/23/1
        val path1 = arrayOf(ChildNumber(23, true), ChildNumber(1, false))
        val subPrivateKeyStr1 = EcKeyUtils.GenSubPrivateKey(mnemonicStr, path1)
        // 使用master衍生出keypair 用于签字 转帐  m/24/1
        val path2 = arrayOf(ChildNumber(24, true), ChildNumber(1, false))
        val subPrivateKeyStr2 = EcKeyUtils.GenSubPrivateKey(mnemonicStr, path2)
        // 加密私钥
        // 生成复合地址
        val address = EcKeyUtils.GenCompositeAddress(subPrivateKeyStr1, subPrivateKeyStr2)
        return arrayOf(mnemonicStr, subPrivateKeyStr1, subPrivateKeyStr2, address)
    }
}