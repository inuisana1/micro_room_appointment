package cn.akira.util;

import cn.akira.pojo.ResponseData;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

public class RsaUtil {

    public static String encrypt(String originalString) throws Exception{
        String pubKeyStr = ConfigUtil.getRsaPublicKey();
        byte[] decodedPubKeyStr = Base64.decodeBase64(pubKeyStr);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decodedPubKeyStr));
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return Base64.encodeBase64String(cipher.doFinal(originalString.getBytes(StandardCharsets.UTF_8)));
    }

    public static String decrypt(String encryptedString) throws Exception{
        String prvKeyStr = ConfigUtil.getRsaPrivateKey();
        byte[] encryptedStringBytes = Base64.decodeBase64(encryptedString.getBytes(StandardCharsets.UTF_8));
        byte[] decodedPrvKey = Base64.decodeBase64(prvKeyStr);
        RSAPrivateKey prvKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decodedPrvKey));
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE,prvKey);
        return new String(cipher.doFinal(encryptedStringBytes));
    }

    @SuppressWarnings("SameParameterValue")
    private static ResponseData getFile(String fileName) {
        ClassLoader classLoader = RsaUtil.class.getClassLoader();
        URL url = classLoader.getResource(fileName);
        ResponseData data = new ResponseData();
        if (url != null) {
            File file = new File(url.getFile());
            if (file.exists()) {
                data.setResource(file);
            } else {
                data.setFlag(false);
                data.setMessage("加载秘钥文件失败");
                System.err.println(data.getMessage());
            }
        } else {
            data.setFlag(false);
            data.setMessage("获取秘钥文件URL失败");
            System.err.println(data.getMessage());
        }
        return data;
    }
}
