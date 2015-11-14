package com.iedcs.security;

/**
 * Created by Andre on 09-11-2015.
 */

import com.iedcs.player.GUI;
import org.apache.commons.io.IOUtils;
import org.hibernate.jpa.HibernatePersistenceProvider;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import java.util.HashMap;


import com.iedcs.persistence.CatalogEntity;
import com.iedcs.persistence.UsercredEntity;
import org.apache.commons.io.IOUtils;
import org.hibernate.jpa.HibernatePersistenceProvider;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import java.io.*;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Andre on 07-11-2015.
 */
public class CipherEbook {

    public static void main(String[] args) throws Exception{
        byte[] device_key_bytes = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x15 };

        String device_key = new String(DeviceKeyFactory.getDevice());
        SecretKey player_key;
        SecretKey user_key;


       /* cipher("C:\\Users\\Andre\\Documents\\ebooks\\prideandprejudice.txt",device_key_bytes);
        decipher1();
        decipher2();
        decipher3();*/

        /*cipher_file("C:\\Users\\Andre\\Documents\\ebooks\\prideandprejudice.txt");
        decipher_file1("C:\\Users\\Andre\\Documents\\ebooks\\prideandprejudice.txt");
        decipher_file2("C:\\Users\\Andre\\Documents\\ebooks\\prideandprejudice.txt");
        decipher_file3("C:\\Users\\Andre\\Documents\\ebooks\\prideandprejudice.txt");*/


       cipher_file("C:\\Users\\Andre\\Documents\\ebooks\\meinkampf.epub");
        decipher_file1("C:\\Users\\Andre\\Documents\\ebooks\\meinkampf.epub");
        decipher_file2("C:\\Users\\Andre\\Documents\\ebooks\\meinkampf.epub");
        decipher_file3("C:\\Users\\Andre\\Documents\\ebooks\\meinkampf.epub");

        File f = new File("C:\\Users\\Andre\\Documents\\ebooks\\meinkampf.epub.aes.aes.aes.dec.dec.dec");


       /* cipher_file("C:\\Users\\Andre\\Documents\\ebooks\\1984.epub");
        decipher_file1("C:\\Users\\Andre\\Documents\\ebooks\\1984.epub");
        decipher_file2("C:\\Users\\Andre\\Documents\\ebooks\\1984.epub");
        decipher_file3("C:\\Users\\Andre\\Documents\\ebooks\\1984.epub");

        File f = new File("C:\\Users\\Andre\\Documents\\ebooks\\1984.epub.aes.aes.aes.dec.dec.dec");*/

        FileInputStream fin = new FileInputStream(f);
        GUI.view(fin);


    }

    public static void cipher_file_1key(String directory, SecretKey file_key)throws Exception{

        AESEncryptor encryptor = new AESEncryptor();
        encryptor.encrypt(directory,file_key);

    }

    public static void decipher_file_1key(String directory,SecretKey file_key)throws Exception{
        AESEncryptor encryptor = new AESEncryptor();
        String true_directory = directory + ".aes";

        encryptor.decrypt(true_directory,file_key);
    }


    public static void cipher_file(String directory)throws Exception{
        //hibernate objects
        PersistenceProvider persistenceProvider = new HibernatePersistenceProvider();
        EntityManagerFactory entityManagerFactory = persistenceProvider.createEntityManagerFactory("NewPersistenceUnit", new HashMap());
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        //

        //make device key
        SecretKey device_key = DeviceKeyFactory.getDeviceKey();

        //get player key
        SecretKey player_key = PlayerKeyFactory.getKey();

        //get user key
        SecretKey user_key = UserKeyFactory.getKey();


        //triple cipher ebook (device,user,player)
        AESEncryptor encryptor = new AESEncryptor();
        encryptor.encrypt(directory,device_key);   //encrypt device key
        String directory_cipher = directory+".aes"; // get directory of ciphered content
        encryptor.encrypt(directory_cipher,user_key);   //encrypt user key
        String directory_cipher2 = directory_cipher+".aes";  //get directory of doubly ciphered content
        encryptor.encrypt(directory_cipher2,player_key);    //encrypt player key



    }

    public static void decipher_file1(String directory)throws Exception{

        AESEncryptor encryptor = new AESEncryptor();
        String true_directory = directory + ".aes.aes.aes";

        encryptor.decrypt(true_directory,PlayerKeyFactory.getKey());
    }

    public static void decipher_file2(String directory)throws Exception{
        String true_directory = directory + ".aes.aes.aes.dec";

        AESEncryptor encryptor = new AESEncryptor();
        encryptor.decrypt(true_directory,UserKeyFactory.getKey());
    }

    public static void decipher_file3(String directory) throws Exception{
        String true_directory = directory + ".aes.aes.aes.dec.dec";

        AESEncryptor encryptor = new AESEncryptor();
        encryptor.decrypt(true_directory,DeviceKeyFactory.getDeviceKey());
    }

    public static void cipher_once(String directory)throws Exception{
        byte[] player_key_bytes = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17 };

        SecretKeySpec key = new SecretKeySpec(player_key_bytes, "AES");

        File febook = new File(directory);
        FileReader freader = new FileReader(febook);
        BufferedReader buffer = new BufferedReader(freader);
        byte[] ebook_bytes = IOUtils.toByteArray(buffer);       // convert ebook to bytes


        byte[] cipher1 =AES.encrypt(key,ebook_bytes);          //first encryption with device key

        File encrypted_book = new File("C:\\Users\\Andre\\Documents\\ebooks\\user_stuff\\encrypted"); //writes encrypted ebook to file
        FileOutputStream fout = new FileOutputStream(encrypted_book);

        fout.write(cipher1);
        fout.close();




    }


    public static void cipher(String directory,byte[] device_key)throws Exception{
        byte[] player_key_bytes = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17 };

        // byte[] player_key_bytes = "1234567891234567".getBytes();
        //byte[] user_key_bytes = "1234123412341234".getBytes();

        byte[] user_key_bytes = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x16 };


        SecretKeySpec key1 = new SecretKeySpec(device_key,"AES"); //make device key
        SecretKeySpec key2 = new SecretKeySpec(user_key_bytes,"AES"); //make user key
        SecretKeySpec key3 = new SecretKeySpec(player_key_bytes,"AES"); //make player_key




        File febook = new File(directory);                      // get ebook
        FileReader freader = new FileReader(febook);
        BufferedReader buffer = new BufferedReader(freader);
        byte[] ebook_bytes = IOUtils.toByteArray(buffer);       // convert ebook to bytes


        byte[] cipher1 =AES.encrypt(key1,ebook_bytes);          //first encryption with device key
        byte[] cipher2 = AES.encrypt(key2,cipher1);             //second encryption with user key
        byte[] cipher3 = AES.encrypt(key3,cipher2);             //third encryption with player key

        File encrypted_book = new File("C:\\Users\\Andre\\Documents\\ebooks\\user_stuff\\encrypted"); //writes encrypted ebook to file
        FileOutputStream fout = new FileOutputStream(encrypted_book);
        fout.write(cipher3);
        fout.close();


    }

    public static void decipher1()throws Exception{
        String directory = "C:\\Users\\Andre\\Documents\\ebooks\\user_stuff\\encrypted";
        File encrypted_book = new File(directory);
        FileReader freader = new FileReader(encrypted_book);
        BufferedReader buffer = new BufferedReader(freader);
        byte[] encrypted_bytes = IOUtils.toByteArray(buffer); //convert ciphertext file to bytes

        byte[] player_key_bytes = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17 };


        SecretKeySpec key3 = new SecretKeySpec(player_key_bytes,"AES"); //make player_key

        byte[] cipher = AES.decrypt(key3,encrypted_bytes);
        File decrypted_book = new File("C:\\Users\\Andre\\Documents\\ebooks\\user_stuff\\dec1");
        decrypted_book.createNewFile();
        FileOutputStream fout = new FileOutputStream(decrypted_book);
        fout.write(cipher);
        fout.close();


    }


    public static void decipher2()throws Exception{
        String directory = "C:\\Users\\Andre\\Documents\\ebooks\\user_stuff\\dec1";
        File encrypted_book = new File(directory);
        FileReader freader = new FileReader(encrypted_book);
        BufferedReader buffer = new BufferedReader(freader);
        byte[] encrypted_bytes = IOUtils.toByteArray(buffer); //convert ciphertext file to bytes

        byte[] user_key_bytes = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x16 };


        SecretKeySpec key2 = new SecretKeySpec(user_key_bytes,"AES"); //make user_key

        byte[] cipher = AES.decrypt(key2,encrypted_bytes);
        File decrypted_book = new File("C:\\Users\\Andre\\Documents\\ebooks\\user_stuff\\dec2");
        decrypted_book.createNewFile();
        FileOutputStream fout = new FileOutputStream(decrypted_book);
        fout.write(cipher);
        fout.close();
    }

    public static void decipher3()throws Exception{
        String directory = "C:\\Users\\Andre\\Documents\\ebooks\\user_stuff\\dec2";
        File encrypted_book = new File(directory);
        FileReader freader = new FileReader(encrypted_book);
        BufferedReader buffer = new BufferedReader(freader);
        byte[] encrypted_bytes = IOUtils.toByteArray(buffer); //convert ciphertext file to bytes

        byte[] device_key_bytes = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x15 };


        SecretKeySpec key1 = new SecretKeySpec(device_key_bytes,"AES"); //make device_key

        byte[] cipher = AES.decrypt(key1,encrypted_bytes);
        File decrypted_book = new File("C:\\Users\\Andre\\Documents\\ebooks\\user_stuff\\dec3.txt");
        decrypted_book.createNewFile();
        FileOutputStream fout = new FileOutputStream(decrypted_book);
        fout.write(cipher);
        fout.close();
    }




}

