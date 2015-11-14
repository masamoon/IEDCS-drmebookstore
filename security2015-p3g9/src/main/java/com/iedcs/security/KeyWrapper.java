package com.iedcs.security;

/**
 * Created by Andre on 12-11-2015.
 */
import com.iedcs.player.GUI;

import java.io.File;
import java.io.FileInputStream;
import java.security.Key;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class KeyWrapper {
    public static void main(String[] args) throws Exception {
        SecretKey user_key = UserKeyFactory.getKey();
        SecretKey device_key = DeviceKeyFactory.getDeviceKey();
        SecretKey player_key = PlayerKeyFactory.getKey();
        SecretKey file_key = FileKeyFactory.getKey();



        //cipher file with file_key
        CipherEbook.cipher_file_1key("C:\\Users\\Andre\\Documents\\ebooks\\meinkampf.epub", file_key);
        //cipher file_key with device key, player key and user key
        byte[] wrap_key = wrapKey_3(device_key,user_key,player_key,file_key);
        //recover filekey
        SecretKey file_key_unwrapped = unwrapKey_3(device_key,user_key,player_key,wrap_key);

        System.out.println(new String(file_key.getEncoded()));
        System.out.println(new String(file_key_unwrapped.getEncoded()));

        //decipher file with unwrapped file_key
        CipherEbook.decipher_file_1key("C:\\Users\\Andre\\Documents\\ebooks\\meinkampf.epub",file_key_unwrapped);

        //open deciphered file with GUI
        File f = new File("C:\\Users\\Andre\\Documents\\ebooks\\meinkampf.epub.aes.dec");
        FileInputStream fin = new FileInputStream(f);
        GUI.view(fin);



    }

    public static byte[] wrapKey(Key wrapKey, Key keyToBeWrapped)throws Exception{
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        // create a wrapper and do the wrapping
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding", "BC");
        KeyGenerator keyGen = KeyGenerator.getInstance("AES", "BC");
        keyGen.init(256);
        //Key wrapKey = keyGen.generateKey();
        cipher.init(Cipher.ENCRYPT_MODE, wrapKey);
        byte[] wrappedKey = cipher.doFinal(keyToBeWrapped.getEncoded());
      //  System.out.println("wrapped  : " + new String(wrappedKey));
        return wrappedKey;
    }

    public static SecretKey unrwrapKey(Key wrapKey, byte[] wrappedKey)throws Exception{
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        // unwrap the wrapped key
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, wrapKey);
        SecretKey key = new SecretKeySpec(cipher.doFinal(wrappedKey), "AES");
      //  System.out.println("unwrapped: " + new String(key.getEncoded()));
        return key;
    }

    public static byte[] wrapKey_3(Key wrapKey1, Key wrapKey2, Key wrapKey3, Key keyToBeWrapped)throws Exception{
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        // create a wrapper and do the wrapping
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding", "BC");
        KeyGenerator keyGen = KeyGenerator.getInstance("AES", "BC");
        keyGen.init(256);
        //Key wrapKey = keyGen.generateKey();
        cipher.init(Cipher.ENCRYPT_MODE, wrapKey1);
        byte[] wrappedKey = cipher.doFinal(keyToBeWrapped.getEncoded());
        //  System.out.println("wrapped  : " + new String(wrappedKey));
        cipher.init(Cipher.ENCRYPT_MODE, wrapKey2);
        byte[] wrappedKey2 = cipher.doFinal(wrappedKey);

        cipher.init(Cipher.ENCRYPT_MODE, wrapKey3);
        byte[] wrappedKey3 = cipher.doFinal(wrappedKey2);


        return wrappedKey3;
    }

    public static SecretKey unwrapKey_3(Key wrapKey1, Key wrapKey2, Key wrapKey3, byte[] wrappedKey)throws Exception{
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        // unwrap the wrapped key
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, wrapKey3);
        byte[] unwrapped1 = cipher.doFinal(wrappedKey);
        cipher.init(Cipher.DECRYPT_MODE, wrapKey2);
        byte[] unwrapped2 = cipher.doFinal(unwrapped1);
        cipher.init(Cipher.DECRYPT_MODE, wrapKey1);
        byte[] unwrapped3 = cipher.doFinal(unwrapped2);

        SecretKey key = new SecretKeySpec(unwrapped3, "AES");

        return key;

    }
}
