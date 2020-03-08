package cn.leiyu.base.utils.encoded;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.Base58;

import java.math.BigInteger;
import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/9/28.
 */



public class EcUtils {
    /**
     * 通过公钥生成钱包地址
     */
    public static String generateAddressByPublickey(ECPublicKey publickey)
    {
        Base58 base58  =new Base58();
        try {
//			System.out.println(publickey);
            String hexStr = formatEcPublicKey(publickey);
//			System.out.println("hex:"+hexStr);
            String address = base58.encode(Hex.decodeHex(hexStr.toCharArray()));
            return "CFWCHAIN"+address;
        } catch (DecoderException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String generateAddress(byte[] addressbytes)
    {
        Base58 base58  =new Base58();
        String address = base58.encode(addressbytes);
//        return "CFWCHAIN"+address;
        //项目更换
        return "OSN"+address;
    }

    /**
     * 格式化公钥
     */
    public static String formatEcPublicKey(ECPublicKey publicKey)
    {
        final int SZIE = 64;
        String res = "04"
                + getStandardSizeInteger(publicKey.getW().getAffineX(),SZIE)
                + getStandardSizeInteger(publicKey.getW().getAffineY(),SZIE);
        return res;
    }

    public static byte[] EcPublicKey2Bytes(ECPublicKey publicKey)
    {
        String str = formatEcPublicKey(publicKey);
        try {
            return Hex.decodeHex(str.toCharArray());
        } catch (Exception e)
        {
            return null;
        }
    }

    private static String getStandardSizeInteger(BigInteger value, int size)
    {
        String hex= value.toString(16);
        if(hex.length()<size)
        {
            int len = size -hex.length();
            String temp = "";
            for(int i=0;i<len;i++)
            {
                temp+="0";
            }
            hex = temp+hex;
        }
        return hex;
    }
//    /**
//     * 创建钱包
//     */
//    public static WalletBean createWallet() {
//        WalletBean bean = new WalletBean();
//        ECPrivateKey privateKey= EcKeyUtils.generateEcPrivateKey();
//        ECPublicKey publicKey = EcKeyUtils.getPublicKeyFromPrivateKey(privateKey);
//        bean.setPrivateKey(privateKey.getS().toString(16));
//        bean.setPublicKey(formatEcPublicKey(publicKey));
//        bean.setAddress(generateAddressByPublickey(publicKey));
//        return bean;
//    }
//
//    /**
//     * 通过16进制的 私钥D，生成钱包
//     * @param privateKey
//     * @return
//     */
//    public static WalletBean createByPrivateKey(String privateKey) {
//        if (privateKey.length() == 64) {
//            WalletBean bean = new WalletBean();
//            try{
//                ECPrivateKey prk = EcKeyUtils.getEcPrivateKeyFromHex(privateKey);
//                ECPublicKey puk = EcKeyUtils.getPublicKeyFromPrivateKey(prk);
//                bean.setPrivateKey(privateKey);
//                bean.setPublicKey(formatEcPublicKey(puk));
//                bean.setAddress(generateAddressByPublickey(puk));
//                return bean;
//            }
//            catch(Exception e)
//            {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }


    public static String CFEncrypt(List<String> addressList, byte[] clearData)
    {
        CFWinCipherData cfcipher = new CFWinCipherData();

        // 加密
        String password = cfcipher.encrypt(clearData);

        // 创建授权
        List<byte[]> auths = new ArrayList<>();
        for (String address: addressList) {
            if (!cfcipher.addAuth(password, address))
                return null;
        }

        // 生成字符串
        return cfcipher.encodeToString();
    }

    public static byte[] CFDecrypt(String privkey, String cipherData)
    {
        // 1.
        CFWinCipherData cfcipher = new CFWinCipherData();
        try {
            return cfcipher.decrypt(cipherData, privkey);
        }catch (Exception e)
        {
            return null;
        }
    }
}

