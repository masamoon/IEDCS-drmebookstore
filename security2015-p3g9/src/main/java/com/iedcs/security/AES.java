package com.iedcs.security;

/**
 * Created by Andre on 07-11-2015.
 */
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES {
    public static void main(String[] args) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
       // byte[] input = "www.java2s.com".getBytes();
        byte[] keyBytes = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17 };

        //byte[] keybytes_player = PlayerKeyFactory.getKey();
        byte[] keybytes_player = "jashdahsdiuasdh".getBytes();
        byte[] keybytes_device = DeviceKeyFactory.getDevice();


        SecretKeySpec key = new SecretKeySpec(keybytes_player, "AES");
        SecretKeySpec key2 = new SecretKeySpec(keybytes_device, "AES");
        SecretKeySpec key3 = new SecretKeySpec(keyBytes, "AES");


        File febook = new File("C:\\Users\\Andre\\Documents\\ebooks\\prideandprejudice.txt"); //input clear text ebook
        FileInputStream inputStream = new FileInputStream(febook);
        //FileOutputStream outputStream = new FileOutputStream(febook);

        byte[] input = IOUtils.toByteArray(inputStream);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(input);

        System.out.println("DIGEST CLEAR: "+new String(md.digest()));



        byte[] cipher1 = encrypt(key,input);
        byte[] cipher2 = encrypt(key2,cipher1);
        byte[] cipher3 = encrypt(key3,cipher1);
        byte[] clear = decrypt(key3,cipher3);
        byte[] clear2 = decrypt(key2,cipher2);
        byte[] clear3 = decrypt(key,cipher1);

        md.update(clear3);
        System.out.println("DIGEST DECRYPT "+new String(md.digest()));
        System.out.println(new String(clear3));

        // System.out.println("DECRYPT:"+ new String(clear3));




    }

    public static byte[] encrypt(SecretKeySpec key, byte[] input )throws Exception{
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "BC");

        cipher.init(Cipher.ENCRYPT_MODE, key);


        byte[] cipherText = new byte[cipher.getOutputSize(input.length)];
        int ctLength = cipher.update(input, 0, input.length, cipherText, 0);
        ctLength += cipher.doFinal(cipherText, ctLength);
       // System.out.println("enc1: "+new String(cipherText));
        //System.out.println(ctLength);

        return cipherText;
    }

    public static byte[] decrypt(SecretKeySpec key, byte[] cipherText)throws Exception{
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "BC");

        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] plainText = new byte[cipher.getOutputSize(cipherText.length)];
        int ptLength = cipher.update(cipherText, 0, cipherText.length, plainText, 0);
        ptLength += cipher.doFinal(plainText, ptLength);
        System.out.println(new String(plainText));
        //System.out.println(ptLength);

        return plainText;
    }

}