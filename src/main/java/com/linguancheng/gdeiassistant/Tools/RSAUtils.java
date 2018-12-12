package com.linguancheng.gdeiassistant.Tools;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAUtils {

    /**
     * 生成RSA密钥对
     *
     * @return
     * @throws NoSuchAlgorithmException
     */
    public KeyPair GenerateRSAKeyPair() throws NoSuchAlgorithmException {
        //使用RSA算法获得密钥对生成器对象KeyPairGenerator
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        //设置密钥长度为2048
        keyPairGenerator.initialize(2048);
        //生成密钥对
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 使用RSA公钥加密内容
     *
     * @param publicKeyByte
     * @param content
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public byte[] EncryptByte(byte[] publicKeyByte, byte[] content) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException {
        //将私钥字节数组转换为PublicKey
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyByte));
        //获取一个加密算法为RSA的加解密器对象Cipher
        Cipher cipher = Cipher.getInstance("RSA");
        //设置为加密模式，并将公钥给Cipher
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        //获得密文并返回
        return cipher.doFinal(content);
    }

    /**
     * 使用RSA私钥解密内容
     *
     * @param privateKeyBytes
     * @param content
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IOException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public byte[] DecryptByte(byte[] privateKeyBytes, byte[] content) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException {
        //将私钥字节数组转换为PrivateKey
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        //获取一个加密算法为RSA的加解密器对象Cipher
        Cipher cipher = Cipher.getInstance("RSA");
        //传递私钥，设置为解密模式
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        //解密器解密密文，获得明文字节数组
        return cipher.doFinal(content);
    }
}
