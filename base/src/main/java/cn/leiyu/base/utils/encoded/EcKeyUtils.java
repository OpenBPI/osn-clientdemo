package cn.leiyu.base.utils.encoded;

import android.util.Base64;
import android.util.Log;

import com.subgraph.orchid.encoders.Hex;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.params.MainNetParams;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;
import cn.leiyu.base.utils.LogUtil;
import io.github.novacrypto.bip39.MnemonicGenerator;
import io.github.novacrypto.bip39.Words;
import io.github.novacrypto.bip39.wordlists.English;

import static cn.leiyu.base.utils.encoded.EcSignUtil.SIGN_ALGORITHM;
import static io.github.novacrypto.hashing.Sha256.sha256;

//import org.bouncycastle.jce.spec.IESParameterSpec;
//import org.bouncycastle.jce.interfaces.ECPrivateKey;
//import org.bouncycastle.jce.interfaces.ECPublicKey;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.math.ec.ECCurve;
//import org.bouncycastle.math.ec.ECPoint;

/**
 * Created by Administrator on 2018/9/28.
 */

public class EcKeyUtils {
    public static final String ALGORITHM = "EC";
    public static final String SPACE_NAME= "prime256v1";// eq prime256v1

    /**
     * 生成助记词
     */
    public static String createMnemonic(){


//        MnemonicCode mc = new MnemonicCode();
//        byte[] entropy = {};
//        mc.toMnemonic(entropy);

        StringBuilder sb = new StringBuilder();
        byte[] entropy = new byte[Words.FIFTEEN.byteLength()];
        new SecureRandom().nextBytes(entropy);
        new MnemonicGenerator(English.INSTANCE).createMnemonic(entropy, sb::append);

        String mnemonics = sb.toString();
        return mnemonics;
    }


    public static boolean  saveFile(byte[] bytes, String fileName, File directory) {
        // 创建String对象保存文件名路径
        try {
            // 创建指定路径的文件
            File file = new File(directory, fileName);
            if(!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }
            // 如果文件不存在
            if (!file.exists()) {
                file.createNewFile();
            }

            // 获取文件的输出流对象
            FileOutputStream outStream = new FileOutputStream(file);
            // 获取字符串对象的byte数组并写入文件流
            outStream.write(bytes);
            // 最后关闭文件输出流
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return  true;
    }

    /**
     * 保存助记词
     * password 密码
     * path 保存文件路径
     */
    public static boolean saveMnemonic(String password, String mnemonic, String dir, String filename){
        //
        if (password == "") {
            return false;
        }

        //byte[] passwordbyte = sha256(password.getBytes());
        //byte[] rawKey = AESUtil.getRawKey(passwordbyte);

        // AES crypto
        byte[] encryptedByteArr = AESUtil.encrypt(password,mnemonic.trim().getBytes());
        String encryptedPwd = new String(encryptedByteArr);
        Log.e("encryptedPwd",encryptedPwd);
        // save

        return saveFile(encryptedByteArr, filename, new File(dir));
    }

    /**
     * 显示助记词
     */
    public static String getMnemonic(String password, String dir, String filename) throws IOException {
        // 打开文件，读取密文

        // 创建文件
        File file = new File(dir,filename);
        if (!file.exists())
        {
            return null;
        }
        long length = file.length();
        if (length > 512 || length <= 16)
        {
            return null;
        }
        FileInputStream in1 = null;
        DataInputStream data_in = null;
        byte[] b = new byte[(int) length];
        try {
            in1 = new FileInputStream(file);
            data_in = new DataInputStream(in1);


            data_in.read(b, 0, (int) length);

            data_in.close();
            in1.close();
        }
        catch (IOException e){
            data_in.close();
            in1.close();
        }

        // AES 解密
        byte[] decryptedByteArr = AESUtil.decrypt(password, b);
        if (decryptedByteArr == null){
            return null;
        }

        // return
        return new String(decryptedByteArr);

    }
    /**
     * 通过助记词的到一个16进制字符串(通过助记词的到私钥)
     */
    public static String mnemonic2PrivateKey(String mnemonic)
    {
        byte[] seed = MnemonicCode.toSeed(Arrays.asList(mnemonic.split(" ")), "");
        //byte[] seed = MnemonicUtils.generateSeed(mnemonic, null);
        byte[] privkeytemp = sha256(seed);

        //BASE64Encoder enc = new BASE64Encoder();
        //String hexStr = enc.encode(privkeytemp);

        String hexStr = bytesToHex(privkeytemp);
        return hexStr;
        // 返回16进制 hex 字符串
    }

    public static String GenMasterPrivateKey(String mnemonic)
    {
        byte[] seed = MnemonicCode.toSeed(Arrays.asList(mnemonic.split(" ")), "");
        //byte[] seed = MnemonicUtils.generateSeed(mnemonic, null);
        DeterministicKey masterPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
        NetworkParameters MAINNET = MainNetParams.get();
        byte[] privkeybytes = masterPrivateKey.serializePrivate(MAINNET);
        if (privkeybytes.length == 78)
        {
            String hexStr = bytesToHex(privkeybytes);
            String hextemp = hexStr.substring(hexStr.length() - 64);
            return hextemp;
        }
        else
        {
            return null;
        }

        // 返回16进制 hex 字符串
    }

    public static String GenSubPrivateKey(String mnemonic, ChildNumber[] path)
    {
        byte[] seed = MnemonicCode.toSeed(Arrays.asList(mnemonic.split(" ")), "");
        //byte[] seed = MnemonicUtils.generateSeed(mnemonic, null);
        DeterministicKey masterPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
        NetworkParameters MAINNET = MainNetParams.get();

        DeterministicHierarchy dh = new DeterministicHierarchy(masterPrivateKey);

        int depth = path.length - 1;
        //ChildNumber chnum = new ChildNumber(2, false);
//        path = new ChildNumber[]{new ChildNumber(0, false),
//                new ChildNumber(2147483647, true),
//                new ChildNumber(1, false),
//                new ChildNumber(2147483646, true),
//                new ChildNumber(2, false)};


        DeterministicKey ehkey = dh.deriveChild(Arrays.asList(path).subList(0, depth),
                false, true, path[depth]);

        byte[] privkeybytes = ehkey.serializePrivate(MAINNET);
        if (privkeybytes.length == 78)
        {
            String hexStr = bytesToHex(privkeybytes);
            String hextemp = hexStr.substring(hexStr.length() - 64);
            return hextemp;
        }
        else
        {
            return null;
        }

    }

    //

    /**
     * 保存私钥
     * @param privkey
     * @param password
     * @param dir
     * @param filename
     * @return
     */
    public static boolean savePrivateKey(String privkey, String password, String dir, String filename)
    {
        if (password == null || password.trim() == "") {
            // 组合json字符串，存储
            return saveFile(privkey.getBytes(), filename, new File(dir));
        }
        // AES crypto
        byte[] encryptedByteArr = AESUtil.encrypt(password,privkey.trim().getBytes());
        String encryptedPwd = new String(encryptedByteArr);
        // save

        return saveFile(encryptedByteArr, filename, new File(dir));
    }

    /**
     * 从文件中获取私钥
     * @param password
     * @param dir
     * @param filename
     * @return
     */
    public static String getPrivateKey(String password, String dir, String filename) throws IOException {
        // 返回16进制 hex 字符串
        // 打开文件，读取密文

        // 创建文件
        File file = new File(dir,filename);
        if (!file.exists()) {
            return "error";
        }
        long length = file.length();
        if (length > 256 || length <= 16)
        {
            return "error";
        }
        FileInputStream in1 = null;
        DataInputStream data_in = null;
        byte[] b = new byte[(int) length];
        try {
            in1 = new FileInputStream(file);
            data_in = new DataInputStream(in1);


            data_in.read(b, 0, (int) length);

            data_in.close();
            in1.close();
        }
        catch (IOException e){
            data_in.close();
            in1.close();
        }

        if (password == null || password.trim() == "")
        {
            return new String(b, "utf-8");
            //return bytesToHex(b);
            //return new String(b);
        }

        // AES 解密
        byte[] decryptedByteArr = AESUtil.decrypt(password, b);

        if (decryptedByteArr == null){
            return "ERROR";
        }

//        String str= new String(b);
//        return str;
        return bytesToHex(decryptedByteArr);

    }

    /**
     * 从私钥得到地址
     * @param privkey
     * @return
     */
    public static String privateKey2Address(String privkey)
    {
        // privkey 2 public
        ECPrivateKey prvatekey = EcKeyUtils.getEcPrivateKeyFromHex(privkey);
        ECPublicKey publickey = EcKeyUtils.getPublicKeyFromPrivateKey(prvatekey);
        return EcUtils.generateAddressByPublickey(publickey);
    }

//    public static String GenCompositeAddress(String privkey, String privKeyShadow)
//    {
//        /*
//        * 0x11
//        * 0x04 65 bytes
//        * 32 bytes hash
//        * */
//        // privkey 2 public
//        ECPrivateKey prvatekey = EcKeyUtils.getEcPrivateKeyFromHex(privkey);
//        ECPublicKey publickey = EcKeyUtils.getPublicKeyFromPrivateKey(prvatekey);
//        byte[] pub1 = EcUtils.EcPublicKey2Bytes(publickey);
//
//        ////////////////////////////////////////////////////////////////////////////////////////////
////        String test = "hello";
////        byte[] data = ECIESEncrypt(publickey,test.getBytes());
////        byte[] datatest = ECIESDecrypt(prvatekey, data);
////        String s = new String(datatest);
//        ////////////////////////////////////////////////////////////////////////////////////////////
//        ECPrivateKey prvatekeyshadow = EcKeyUtils.getEcPrivateKeyFromHex(privKeyShadow);
//        ECPublicKey publickeyshadow = EcKeyUtils.getPublicKeyFromPrivateKey(prvatekeyshadow);
//        byte[] pub2 = EcUtils.EcPublicKey2Bytes(publickeyshadow);
//        byte[] pub2hash = sha256(pub2);
//
//        byte[] address = new byte[1+65+32];
//        address[0] = 0x11;
//        System.arraycopy(pub1, 0, address, 1,65);
//        System.arraycopy(pub2hash, 0, address, 66,32);
//        return EcUtils.generateAddress(address);
//    }
    public static String GenCompositeAddress(String privkey, String privKeyShadow)
    {
        /*
         * 0x1000
         * 0x04 65 bytes
         * 32 bytes hash
         * */
        // privkey 2 public
        ECPrivateKey prvatekey = EcKeyUtils.getEcPrivateKeyFromHex(privkey);
        ECPublicKey publickey = EcKeyUtils.getPublicKeyFromPrivateKey(prvatekey);
        byte[] pub1 = EcUtils.EcPublicKey2Bytes(publickey);
    ////////////////////////////////////////////////////////////////////////////////////////////
    //
        String test = "hello";
    //        byte[] data = ECIESEncrypt(publickey,test.getBytes());
    //        byte[] datatest = ECIESDecrypt(prvatekey, data);
    //        String s = new String(datatest);
    ////////////////////////////////////////////////////////////////////////////////////////////

        ECPrivateKey prvatekeyshadow = EcKeyUtils.getEcPrivateKeyFromHex(privKeyShadow);
        ECPublicKey publickeyshadow = EcKeyUtils.getPublicKeyFromPrivateKey(prvatekeyshadow);
        byte[] pub2 = EcUtils.EcPublicKey2Bytes(publickeyshadow);
        byte[] pub2hash = sha256(pub2);

        byte[] address = new byte[2+65+32];
        address[0] = 0x10;
        address[1] = 0;
        System.arraycopy(pub1, 0, address, 2,65);
        System.arraycopy(pub2hash, 0, address, 67,32);
        return EcUtils.generateAddress(address);
    }



    /**
     * 签名
     * @param privkey
     * @param data
     * @return
     * @throws Exception
     */
    public static byte [] sign(String privkey, byte[] data) throws Exception {
        ECPrivateKey prvatekey = EcKeyUtils.getEcPrivateKeyFromHex(privkey);
        byte[] signdata = EcSignUtil.signData(data, prvatekey);
        return signdata;
    }

    /**
     * 签名
     * @param privkey
     * @param data
     * @return BASE64编码的字符串
     * @throws Exception
     */
    public static String signReturnBase64(String privkey, byte[] data) throws Exception {
        ECPrivateKey prvatekey = EcKeyUtils.getEcPrivateKeyFromHex(privkey);
        byte[] signdata = EcSignUtil.signData(data, prvatekey);
        BASE64Encoder base64Encoder = new BASE64Encoder();
        return base64Encoder.encode(signdata);
    }
    public static String signReturnBase58(String privkey, byte[] data) throws Exception {
        ECPrivateKey prvatekey = EcKeyUtils.getEcPrivateKeyFromHex(privkey);
        byte[] signdata = EcSignUtil.signData(data, prvatekey);
        LogUtil.e("test", bytesToHex(signdata));
        return Base58.encode(signdata);
    }
    public static String signReturnBase64Url(String privkey, byte[] data) throws Exception {
        ECPrivateKey prvatekey = EcKeyUtils.getEcPrivateKeyFromHex(privkey);
        byte[] signdata = EcSignUtil.signData(data, prvatekey);
        return Base64.encodeToString(signdata, Base64.URL_SAFE|Base64.NO_WRAP);
    }

    /**
     * 签名
     * @param privkey
     * @param data
     * @return Hex编码的字符串
     * @throws Exception
     */
    public static String signReturnHexString(String privkey, byte[] data) throws Exception {
        ECPrivateKey prvatekey = EcKeyUtils.getEcPrivateKeyFromHex(privkey);
        byte[] signdata = EcSignUtil.signData(data, prvatekey);
        return new String(Hex.encode(signdata));
    }

    /**
     * 验签
     * @param data
     * @param address
     * @param sign
     * @return
     * @throws Exception
     */
    public static boolean verify(byte[] data, String address , String sign) throws Exception {
        BASE64Decoder base64Decoder = new BASE64Decoder();
        byte[] decodeBuffer = base64Decoder.decodeBuffer(sign);
        return EcSignUtil.verifySign(data, address, decodeBuffer);
    }

    public static boolean verifySign(byte[] data, String addr, byte[] sig) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        //Signature ecdsaVerify = Signature.getInstance(SIGN_ALGORITHM, new BouncyCastleProvider());
        Signature ecdsaVerify = Signature.getInstance(SIGN_ALGORITHM);
        ECPublicKey pkey = getPulicKeyFromAddressNew(addr);
        ecdsaVerify.initVerify(pkey);
        ecdsaVerify.update(data);
        return ecdsaVerify.verify(sig);
    }
    /**
     * 生成私钥
     */
    public static ECPrivateKey generateEcPrivateKey()
    {
        try {
            Provider provider = new BouncyCastleProvider();
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(ALGORITHM,provider);
            kpg.initialize(new ECGenParameterSpec(SPACE_NAME), new SecureRandom());
            KeyPair keyPair = kpg.generateKeyPair();
            return (ECPrivateKey)keyPair.getPrivate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 从16进制的字符中获取私钥
     * @param hexStr
     * @return
     */
    public static ECPrivateKey getEcPrivateKeyFromHex(String hexStr)
    {
        try {
            //BASE64Decoder dec = new BASE64Decoder();
            //byte[] hexdata = dec.decodeBuffer(hexStr);
            //String hexString = bytesToHex(hexdata);

            Provider provider = new BouncyCastleProvider();

            org.bouncycastle.jce.spec.ECNamedCurveParameterSpec spec = org.bouncycastle.jce.ECNamedCurveTable.getParameterSpec(SPACE_NAME);
            KeyFactory kf = KeyFactory.getInstance(ALGORITHM, provider);
            org.bouncycastle.jce.spec.ECNamedCurveSpec params = new org.bouncycastle.jce.spec.ECNamedCurveSpec(SPACE_NAME, spec.getCurve(), spec.getG(), spec.getN());
            BigInteger s = new BigInteger(hexStr, 16);
            ECPrivateKeySpec keySpec =new ECPrivateKeySpec(s, params);
            ECPrivateKey pk = (ECPrivateKey)kf.generatePrivate(keySpec);
            return pk;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从私钥中获取公钥
     */
    public static ECPublicKey getPublicKeyFromPrivateKey(ECPrivateKey privateKey) {
        // prime256v1
        try {
            Provider provider = new BouncyCastleProvider();

            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, provider);
            org.bouncycastle.jce.spec.ECParameterSpec ecSpec = org.bouncycastle.jce.ECNamedCurveTable.getParameterSpec(SPACE_NAME);
            org.bouncycastle.math.ec.ECPoint Q = ecSpec.getG().multiply(((org.bouncycastle.jce.interfaces.ECPrivateKey) privateKey).getD());
            org.bouncycastle.jce.spec.ECPublicKeySpec pubSpec = new org.bouncycastle.jce.spec.ECPublicKeySpec(Q, ecSpec);
            ECPublicKey publicKey= (ECPublicKey) keyFactory.generatePublic(pubSpec);
            return publicKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从16进制的字符中获取公钥
     *
     */
    public static ECPublicKey getPublicKeyFromHex(byte[] pubKey) {
        ECPublicKey pk;
        try {
            Provider provider = new BouncyCastleProvider();

//			byte[] pubKey = Hex.decodeHex(hexstr.toCharArray());
//            byte[] pubKey = Hex.decode(new String(hexstr.toCharArray()));
            org.bouncycastle.jce.spec.ECNamedCurveParameterSpec spec = org.bouncycastle.jce.ECNamedCurveTable.getParameterSpec(SPACE_NAME);
            KeyFactory kf = KeyFactory.getInstance(ALGORITHM, provider);
            org.bouncycastle.jce.spec.ECNamedCurveSpec params = new org.bouncycastle.jce.spec.ECNamedCurveSpec(SPACE_NAME, spec.getCurve(), spec.getG(), spec.getN());
            ECPoint point =  org.bouncycastle.jce.ECPointUtil.decodePoint(params.getCurve(), pubKey);
            ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, params);
            pk = (ECPublicKey) kf.generatePublic(pubKeySpec);
            return pk;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static ECPublicKey getPulicKeyFromAddressNew(String address) {//
        String flag = address.substring(0, 3);
        if (!flag.equals("OSN")) {
            System.out.println("error osn id");
            return null;
        }
        // decode base58
        String base58str = address.substring(3);
        //Base58 base58 = new Base58();
        try {
            byte[] data = Base58.decode(base58str);
            byte[] pub = null;
            System.out.println("hexKey: " + org.bouncycastle.util.encoders.Hex.toHexString(data));
            if (data[0] == 0x10 && data[1] == 0) {
                if (data[2] == 4 && data.length >= 67) {
                    pub = new byte[65];
                    System.arraycopy(data, 2, pub, 0, 65);
                }
            }
            if (pub != null) {
                System.out.println("pub: " + org.bouncycastle.util.encoders.Hex.toHexString(pub));
                // 转化成公钥
                return getPublicKeyFromHex(pub);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
    public static ECPublicKey getPulicKeyFromAddress(String address){
        //
        String flag = address.substring(0, 8);
        if (!flag.equals("CFWCHAIN"))
            return null;
        // decode base58
        String base58str = address.substring(8);
        Base58 base58  =new Base58();
        try {
            byte[] data = base58.decode(base58str);
            byte[] pub = null;
            if (data[0]==0x11)
            {
                if (data[1]==4 && data.length >= 66)
                {
                    pub = new byte[65];
                    System.arraycopy(data,1, pub,0, 65);
                }
            } else if (data[0]==4 && data.length >= 65){
                pub = new byte[65];
                System.arraycopy(data,0, pub,0, 65);
            }

            if (pub != null)
            {
                // 转化成公钥
                return getPublicKeyFromHex(pub);
            }
        } catch (Exception e)
        {
            return null;
        }
        return null;
    }

    /**
     * 字节数组转16进制
     * @param bytes 需要转换的byte数组
     * @return  转换后的Hex字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if(hex.length() < 2){
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }
    public static String ECEncrypt(ECPublicKey publicKey, byte[] data){
        byte[] aesKey = new byte[16];
        byte[] aesIV = new byte[16];
        Random random = new Random();
        for(int i = 0; i < 16; ++i){
            aesKey[i] = (byte)random.nextInt(256);
            aesIV[i] = 0;
        }
        try {
            IvParameterSpec iv = new IvParameterSpec(aesIV);
            SecretKeySpec key = new SecretKeySpec(aesKey, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encData = cipher.doFinal(data);

            byte[] encKey = new byte[32];
            System.arraycopy(aesKey,0,encKey,0,16);
            System.arraycopy(aesIV,0,encKey,16,16);
            byte[] encECKey = ECIESEncrypt(publicKey, encKey);

            byte[] eData = new byte[encECKey.length+encData.length+2];
            eData[0] = (byte)(encECKey.length&0xff);
            eData[1] = (byte)((encECKey.length)>>8&0xff);
            System.arraycopy(encECKey,0,eData,2,encECKey.length);
            System.arraycopy(encData,0,eData,encECKey.length+2,encData.length);
            return Base58.encode(eData);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static String ECDecrypt(ECPrivateKey privateKey, String data){
        try {
            byte[] rawData = Base58.decode(data);
            short keyLength = (short)((rawData[0]&0xff)|((rawData[1]&0xff)>>8));
            byte[] ecData = new byte[keyLength];
            System.arraycopy(rawData,2,ecData,0,keyLength);
            ecData = ECIESDecrypt(privateKey, ecData);

            byte[] aesKey = new byte[16];
            byte[] aesIV = new byte[16];
            byte[] aesData = new byte[rawData.length-keyLength-2];
            System.arraycopy(ecData,0,aesKey,0,16);
            System.arraycopy(ecData,16,aesIV,0,16);
            System.arraycopy(rawData,keyLength+2,aesData,0,rawData.length-keyLength-2);

            IvParameterSpec iv = new IvParameterSpec(aesIV);
            SecretKeySpec key = new SecretKeySpec(aesKey, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] decData = cipher.doFinal(aesData);
            return new String(decData);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static byte[] ECIESEncrypt(ECPublicKey pubkey, byte[] raw){
        try {
            //Cipher cipher = Cipher.getInstance("ECIESwithAES/NONE/PKCS7Padding", "BC");
            Cipher cipher = Cipher.getInstance("ECIESwithAES/NONE/PKCS7Padding",
                    new BouncyCastleProvider());
            cipher.init(Cipher.ENCRYPT_MODE, pubkey);
            //cipher.init(Cipher.ENCRYPT_MODE, pubkey);
            byte[] cipherText = cipher.doFinal(raw);
            return cipherText;
        } catch (Exception e)
        {
            return null;
        }

    }

    public static byte[] ECIESDecrypt(ECPrivateKey privateKey, byte[] raw){
        try {
            Cipher cipher = Cipher.getInstance(
                    "ECIESwithAES/NONE/PKCS7Padding",
                    new BouncyCastleProvider());
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] cipherText = cipher.doFinal(raw);
            return cipherText;
        } catch (Exception e)
        {
            return null;
        }
    }

}


