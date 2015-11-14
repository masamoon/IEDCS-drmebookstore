package com.iedcs.security;

/**
 * Created by Andre on 09-11-2015.
 */
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


public class AESEncryptor {



    public void encrypt(String fname,SecretKey key) throws Exception{
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);  //using AES-256
       // SecretKey key = keyGen.generateKey();  //generating key
        Cipher aesCipher = Cipher.getInstance("AES");  //getting cipher for AES
        aesCipher.init(Cipher.ENCRYPT_MODE, key);  //initializing cipher for encryption with key

        //creating file output stream to write to file
        try(FileOutputStream fos = new FileOutputStream(fname+".aes")){
            //creating object output stream to write objects to file
            ObjectOutputStream oos = new ObjectOutputStream(fos);
         //   oos.writeObject(key);  //saving key to file for use during decryption

            //creating file input stream to read contents for encryption
            try(FileInputStream fis = new FileInputStream(fname)){
                //creating cipher output stream to write encrypted contents
                try(CipherOutputStream cos = new CipherOutputStream(fos, aesCipher)){
                    int read;
                    byte buf[] = new byte[4096];
                    while((read = fis.read(buf)) != -1)  //reading from file
                        cos.write(buf, 0, read);  //encrypting and writing to file
                }
            }
        }

    }

    public void decrypt(String fname, SecretKey key)throws Exception{
       // SecretKey key =null;

        //creating file input stream to read from file
        try(FileInputStream fis = new FileInputStream(fname)){
            //creating object input stream to read objects from file
            ObjectInputStream ois = new ObjectInputStream(fis);
          //  key = (SecretKey)ois.readObject();  //reading key used for encryption

            Cipher aesCipher = Cipher.getInstance("AES");  //getting cipher for AES
            aesCipher.init(Cipher.DECRYPT_MODE, key);  //initializing cipher for decryption with key
            //creating file output stream to write back original contents
            try(FileOutputStream fos = new FileOutputStream(fname+".dec")){
                //creating cipher input stream to read encrypted contents
                try(CipherInputStream cis = new CipherInputStream(fis, aesCipher)){
                    int read;
                    byte buf[] = new byte[4096];
                    while((read = cis.read(buf)) != -1)  //reading from file
                        fos.write(buf, 0, read);  //decrypting and writing to file
                }
            }
        }

    }

    public static void main(String[] args) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);  //using AES-256

        AESEncryptor obj = new AESEncryptor();
        SecretKey key = keyGen.generateKey();
        SecretKey key2 = keyGen.generateKey();
        SecretKey key3 = keyGen.generateKey();

        obj.encrypt("C:\\Users\\Andre\\Documents\\ebooks\\prideandprejudice.txt",key);
        obj.encrypt("C:\\Users\\Andre\\Documents\\ebooks\\prideandprejudice.txt.aes",key2);
        obj.encrypt("C:\\Users\\Andre\\Documents\\ebooks\\prideandprejudice.txt.aes.aes",key3);
        obj.decrypt("C:\\Users\\Andre\\Documents\\ebooks\\prideandprejudice.txt.aes.aes.aes",key3);
        obj.decrypt("C:\\Users\\Andre\\Documents\\ebooks\\prideandprejudice.txt.aes.aes.aes.dec",key2);
        obj.decrypt("C:\\Users\\Andre\\Documents\\ebooks\\prideandprejudice.txt.aes.aes.aes.dec.dec",key);
    }

}