package cn.leiyu.base.utils.encoded;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static io.github.novacrypto.hashing.Sha256.sha256;

/**
 * Created by Administrator on 2018/9/28.
 */

public class AESUtil {
    /**
     * @param rawKey
     *            密钥
     * @param clearPwd
     *            明文字符串
     * @return 密文字节数组
     */
    public static byte[] encrypt(String password, byte[] srcData) {
        try {
            byte[] passwordbyte = sha256(password.getBytes());
            byte[] keyData = new byte[16];
            System.arraycopy(passwordbyte, 0, keyData, 0, 16);
            byte[] ivData = new byte[16];
            System.arraycopy(passwordbyte, 15, ivData, 0, 16);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec skeySpec = new SecretKeySpec(keyData, "AES");

            IvParameterSpec iv = new IvParameterSpec(ivData);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(srcData);
            return encrypted;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @param encrypted
     *            密文字节数组
     * @param rawKey
     *            密钥
     * @return 解密后的字符串
     */
    public static byte[] decrypt(String password, byte[] rawData) {
        try {
            byte[] passwordbyte = sha256(password.getBytes());
            byte[] keyData = new byte[16];
            System.arraycopy(passwordbyte, 0, keyData, 0, 16);
            byte[] ivData = new byte[16];
            System.arraycopy(passwordbyte, 15, ivData, 0, 16);

            SecretKeySpec skeySpec = new SecretKeySpec(keyData, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(ivData);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(rawData);
            return original;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @param seed种子数据
     * @return 密钥数据
     */
    public static byte[] getRawKey(byte[] seed) {
        byte[] rawKey = null;
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(seed);
            // AES加密数据块分组长度必须为128比特，密钥长度可以是128比特、192比特、256比特中的任意一个
            kgen.init(128, secureRandom);
            SecretKey secretKey = kgen.generateKey();
            rawKey = secretKey.getEncoded();
        } catch (NoSuchAlgorithmException e) {
        }
        return rawKey;
    }
}
