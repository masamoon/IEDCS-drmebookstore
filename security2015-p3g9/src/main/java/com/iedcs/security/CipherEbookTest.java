package com.iedcs.security;

import com.iedcs.player.GUI;
import com.iedcs.web.HTTPMethods;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;

/**
 * Created by Andre on 13-11-2015.
 */
public class CipherEbookTest {
    public static void main(String[] args)throws Exception{
       /* byte[] file_key_wrapped = FileKeyFactory.wrappedKey();  //Wrap File Key
        System.out.println("WRAPPED FILE KEY");
        System.out.println(new String(file_key_wrapped));
        File f = new File("C:\\Users\\Andre\\Documents\\ebooks\\meinkampf.epub");
        File fout = new File("C:\\Users\\Andre\\Documents\\ebooks\\meinkampfwheader.epub");
        File fout_noheader = new File("C:\\Users\\Andre\\Documents\\ebooks\\meinkampfrmheader.epub");
        CipherEbook.decipher_file_1key("C:\\Users\\Andre\\Documents\\ebooks\\meinkampf.epub",FileKeyFactory.getKey());
        MakeHeader.make(f,file_key_wrapped,fout);
        byte[] header = MakeHeader.removeHeader(fout,fout_noheader);

        FileInputStream fin = new FileInputStream(fout_noheader);
        GUI.view(fin);*/

     SecretKey user_key = UserKeyFactory.getKey();
     SecretKey device_key = DeviceKeyFactory.getDeviceKey();
     SecretKey player_key = PlayerKeyFactory.getKey();
     SecretKey file_key = FileKeyFactory.getKey();

        String ebook = "meinkampf.epub";

       System.out.println("UNWRAPPED FILE KEY");
       System.out.println(new String(FileKeyFactory.getKey().getEncoded()));

       // get request: cipher ebook on server

        String get = HTTPMethods.sendGet("http://localhost:8080/rest/hello/"+ebook);
        File f = new File("C:\\Users\\Andre\\Documents\\ebooks\\"+ebook+"h.aes");    // cipher ebook with crypto header
        File fout = new File("C:\\Users\\Andre\\Documents\\ebooks\\"+ebook+".aes");  // cipher ebook without crypto header
        byte[] header = MakeHeader.readHeader_bytes(f);                                    // read header
        Base64.Encoder encoder = Base64.getEncoder();                                 // encode header with base64
        //byte[] header_encoded = encoder.encode(header);                               //

        MakeHeader.removeHeader(f,fout);                                             //remove header from cipher ebook

      SecretKey first_key =  KeyWrapper.unwrap_playerKey(header);     // decipher with player key
     //
    // byte[] wrap_key = KeyWrapper.wrapKey_3(device_key, user_key, player_key, file_key);
     //byte[] wrap_key_encoded = encoder.encode(wrap_key);
     //
       byte[] first_key_encoded = encoder.encode(first_key.getEncoded());
        HTTPMethods.sendPost("http://localhost:8080/rest/hello/dec",new String( first_key_encoded));           //send header to recover key
        String resp = HTTPMethods.sendGet("http://localhost:8080/rest/hello/receive");   // get recovered key

       // byte[] key_bytes = resp.getBytes();
     System.out.println("RESP");
     System.out.println(resp);
     Base64.Decoder decoder = Base64.getDecoder();
     byte[] key_bytes = decoder.decode(resp);
     System.out.println("USER KEY UNWRAP");
     System.out.println(new String(key_bytes));
       SecretKey fkey = KeyWrapper.unwrap_deviceKey(key_bytes);  //decipher with device key

       // SecretKey fkey = new SecretKeySpec(key_bytes,"AES");
        System.out.println("RECOVERED KEY");
        System.out.println(new String(fkey.getEncoded()));
        CipherEbook.decipher_file_1key("C:\\Users\\Andre\\Documents\\ebooks\\" + ebook,fkey);


       File fdeciph = new File("C:\\Users\\Andre\\Documents\\ebooks\\" + ebook+".aes.dec");
       FileInputStream fstream = new FileInputStream(fdeciph);
       GUI.view(fstream);


    }
}
