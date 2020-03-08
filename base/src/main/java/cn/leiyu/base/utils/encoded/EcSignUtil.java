package cn.leiyu.base.utils.encoded;

import org.apache.commons.codec.binary.Base64;
import org.bitcoinj.core.Base58;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NullCipher;

import Decoder.BASE64Decoder;

/**
 * Created by Administrator on 2018/9/28.
 */

public class EcSignUtil {
    public static final String ALGORITHM = "EC";
    public static final String SIGN_ALGORITHM = "SHA256withECDSA";
    public static final String ECDSA = "ECDSA";
    public static final String PRIME_256V1 = "prime256v1";

    private static PublicKey getPublicKeyFromBytes(byte[] pubKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec(PRIME_256V1);
        KeyFactory kf = KeyFactory.getInstance(ECDSA, new BouncyCastleProvider());
        ECNamedCurveSpec params = new ECNamedCurveSpec(PRIME_256V1, spec.getCurve(), spec.getG(), spec.getN());
        ECPoint point =  ECPointUtil.decodePoint(params.getCurve(), pubKey);
        ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, params);
        ECPublicKey pk = (ECPublicKey) kf.generatePublic(pubKeySpec);
        return pk;
    }
    /**
     * 得到公钥
     *
     * @param key 密钥字符串（经过base64编码）
     * @throws Exception
     */
    public static PublicKey getPublicKey(String key) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes;
        // CFWCHAIN
        //
        String base58str = key.substring(8,key.length());

        Base58 dec = new Base58();
        keyBytes = dec.decode(base58str);

        PublicKey publicKey = getPublicKeyFromBytes(keyBytes);
        return publicKey;

        //Base64 base64 = new Base64();
        //keyBytes = base64.decode(key);
//        keyBytes = (new BASE64Decoder()).decodeBuffer(key);

//        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
//        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
//        PublicKey publicKey = keyFactory.generatePublic(keySpec);
//        return publicKey;
    }

    /**
     * 得到私钥
     *
     * @param key 密钥字符串（经过base64编码）
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(String key) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes,keyBytes2;

        //Base64 base64 = new Base64();
        //keyBytes2 = base64.decode("de65976ea32b0d5bc177e3bdf197f2fe81bb0002c7eb4ab9af032bbc40ab97a7");
        BASE64Decoder dec = new BASE64Decoder();
        keyBytes = dec.decodeBuffer(key);
//        keyBytes = (new BASE64Decoder()).decodeBuffer(key);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

    /**
     * 得到密钥字符串（经过base64编码）
     *
     * @return
     */
    public static String getKeyString(Key key) {
        byte[] keyBytes = key.getEncoded();
        Base64 base64 = new Base64();
        String s = base64.encodeAsString(keyBytes);
//        String s = (new BASE64Encoder()).encode(keyBytes);
        return s;
    }
    /**
     * 加密
     *
     * @param data      数据
     * @param publicKey 公钥
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws InvalidKeySpecException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public static byte[] encrypt(byte[] data, ECPublicKey publicKey) throws InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = new NullCipher();

        ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(publicKey.getW(), publicKey.getParams());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, ecPublicKeySpec.getParams());
        return cipher.doFinal(data);
    }

    /**
     * 加密
     *
     * @param data      数据
     * @param publicKey 公钥
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws InvalidKeySpecException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public static byte[] encrypt(byte[] data, String publicKey) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        ECPublicKey pubKey = (ECPublicKey) getPublicKey(publicKey);
        return encrypt(data, pubKey);
    }

    /**
     * 解密
     *
     * @param data       数据
     * @param privateKey 私钥
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws InvalidKeySpecException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public static byte[] decrypt(byte[] data, ECPrivateKey privateKey) throws InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = new NullCipher();

        ECPrivateKeySpec ecPrivateKeySpec = new ECPrivateKeySpec(privateKey.getS(), privateKey.getParams());
        cipher.init(Cipher.ENCRYPT_MODE, privateKey, ecPrivateKeySpec.getParams());
        return cipher.doFinal(data);
    }

    /**
     * 解密
     *
     * @param data       数据
     * @param privateKey 私钥
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws InvalidKeySpecException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public static byte[] decrypt(byte[] data, String privateKey) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        ECPrivateKey priKey = (ECPrivateKey) getPrivateKey(privateKey);
        return decrypt(data, priKey);
    }

    /**
     * 生产密钥对
     *
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static KeyPair generateKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(256);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }

    /**
     * 验签名
     *
     * @param data 数据
     * @param key  公钥
     * @param sig  签名
     * @return
     * @throws Exception
     */
    public static boolean verifySign(byte[] data, PublicKey key, byte[] sig) throws Exception {
        Signature signer = Signature.getInstance(SIGN_ALGORITHM);
        signer.initVerify(key);
        signer.update(data);
        return (signer.verify(sig));
    }

    /**
     * 验签名
     *
     * @param data      数据
     * @param publicKey 公钥
     * @param sig       签名
     * @return
     * @throws Exception
     */
    public static boolean verifySign(byte[] data, String publicKey, byte[] sig) throws Exception {
        PublicKey key = getPublicKey(publicKey);
        return verifySign(data, key, sig);
    }

    /**
     * 签名数据
     *
     * @param data 要签名的数据
     * @param key  私钥
     * @return
     * @throws Exception
     */
    public static byte[] signData(byte[] data, PrivateKey key) throws Exception {
        Signature signer = Signature.getInstance(SIGN_ALGORITHM);
        signer.initSign(key);
        signer.update(data);
        return (signer.sign());
    }

    /**
     * 签名数据
     *
     * @param data       要签名的数据
     * @param privateKey 私钥
     * @return
     * @throws Exception
     */
    public static byte[] signData(byte[] data, String privateKey) throws Exception {
        PrivateKey key = getPrivateKey(privateKey);
        return signData(data, key);
    }
}

