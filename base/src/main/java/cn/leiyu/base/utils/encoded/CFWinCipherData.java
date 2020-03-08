package cn.leiyu.base.utils.encoded;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class CFWinCipherData {
    public byte[] cipherData;
    public List<byte[]> auths = new ArrayList<>();
    final String MessageBegin = "-----BEGIN CFWIN MESSAGE-----";
    final String MessageEnd = "-----END CFWIN MESSAGE-----";


    private int getTotalSize(){
        int length = (auths.size() + 1) * 3;
        length += cipherData.length;
        for (byte[] data:auths
             ) {
            length += data.length;
        }
        return length;
    }

    public static byte[] short2Byte(short x)
    {
        byte high = (byte) (0x00FF & (x>>8));//定义第一个byte
        byte low = (byte) (0x00FF & x);//定义第二个byte
        byte[] bytes = new byte[2];
        bytes[0] = high;
        bytes[1] = low;
        return bytes;
    }

    public static short byte2short(byte[] bytes){
        byte high = bytes[0];
        byte low = bytes[1];
        short x = (short)(((high & 0x00FF) << 8) | (0x00FF & low));
        return x;
    }

    private byte[] createBlock(byte[] data){
        if (data.length > 65534 / 2){
            return null;
        }
        byte[] block = new byte[data.length + 3];
        block[0] = 0x02;
        byte[] length = short2Byte((short)data.length);
        block[1] = length[0];
        block[2] = length[1];
        System.arraycopy(data, 0, block, 3, data.length);
        return block;
    }

    private byte[] encode(){
        // 块 02 length data
        // 1 计算总长
        byte[] hexData = new byte[getTotalSize()];

        // 2 cipher
        byte[] cipherblock = createBlock(cipherData);
        int pos = 0;
        System.arraycopy(cipherblock,0, hexData, pos, cipherblock.length);
        pos += cipherblock.length;

        // 3 auth
        for (byte[] auth: auths) {
            byte[] authblock = createBlock(auth);
            System.arraycopy(authblock,0, hexData, pos, authblock.length);
            pos += authblock.length;
        }
        return hexData;
    }

    public String encodeToString(){
        byte[] data = encode();
        if (data == null)
        {
            return null;
        }
        // base64
        String rawStr = android.util.Base64.encodeToString(data, android.util.Base64.URL_SAFE| android.util.Base64.NO_WRAP);
        return MessageBegin + "\n" + rawStr + "\n" +  MessageEnd;
    }

    public String encrypt(byte[] data){
        // 1 生成随机密码
        UUID uuid = UUID.randomUUID();
        if (null == data || data.length == 0)
            return null;
        // 2 AES 加密
        byte[] AESCipher =  AESUtil.encrypt(uuid.toString(), data);
        if (null == AESCipher)
            return null;
        cipherData = AESCipher;
        return uuid.toString();
    }

    public boolean addAuth(String password, String address){
        // 解析address
        ECPublicKey pub = EcKeyUtils.getPulicKeyFromAddress(address);
        // ECIES加密
        byte[] auth = EcKeyUtils.ECIESEncrypt(pub, password.getBytes());
        if (auth != null)
        {
            auths.add(auth);
            return true;
        }
        return false;
    }

    private boolean decode (String cipher){
        // 1. 去掉 begin 和 end
        int pos = cipher.indexOf(MessageBegin);
        if (pos != -1)
            pos += MessageBegin.length();
        String ciphertemp = cipher.substring(pos);
        pos = ciphertemp.lastIndexOf(MessageEnd);
        ciphertemp = ciphertemp.substring(0, pos).trim();
        // 2. base64 decode

        byte[] raw = android.util.Base64.decode(ciphertemp,android.util.Base64.URL_SAFE |android.util.Base64.NO_WRAP);
        // 3. 解析出cipherdata
        byte[] length = new byte[2];
        pos = 0;
        if (raw == null)
            return false;
        if (raw[0] == 2)
        {
            length[0] = raw[1];
            length[1] = raw[2];
            short len = byte2short(length);

            cipherData = new byte[len];
            // 还应该判断后面的数据是否足够长
            System.arraycopy(raw,3, cipherData, 0, len);
            pos = cipherData.length + 3;
        }
        // 4. 解析auths
        while (pos < raw.length - 1)
        {
            if (raw[pos] == 2)
            {
                length[0] = raw[pos+1];
                length[1] = raw[pos+2];
                short len = byte2short(length);

                byte[] data = new byte[len];
                // 还应该判断后面的数据是否足够长
                System.arraycopy(raw,pos + 3, data, 0, len);
                pos += (data.length + 3);
                auths.add(data);
            }
        }
        return true;
    }

    private String getPassword(String prv)
    {
        // 1. private string 2 ec private
        ECPrivateKey prvkey =EcKeyUtils.getEcPrivateKeyFromHex(prv);
        if (prvkey == null)
            return null;
        // 2. 解密
        for (byte[] data: auths) {
            byte[] clearData = EcKeyUtils.ECIESDecrypt(prvkey, data);
            if (clearData != null)
            {
                return new String(clearData);
            }
        }
        return null;
    }

    public byte[] decrypt(String cipher, String prv)
    {
        // 1 解析cipher
        if (!decode(cipher))
            return null;
        // 2 解密password
        String pwd = getPassword(prv);
        // 3 解密密文
        return AESUtil.decrypt(pwd, cipherData);
    }

}
