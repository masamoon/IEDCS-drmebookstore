package com.iedcs.security;

import com.iedcs.player.GUI;
import com.iedcs.web.HTTPMethods;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;

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


        String get = HTTPMethods.sendGet("http://localhost:8080/rest/hello/meinkampf");
        File f = new File("C:\\Users\\Andre\\Documents\\ebooks\\meinkampfh.epub");
        File fout = new File("C:\\Users\\Andre\\Documents\\ebooks\\meinkampfr.epub");
        String header = MakeHeader.readHeader(f);
        System.out.println("GET RESULT");
        System.out.println(get);
        MakeHeader.removeHeader(f,fout); 
        HTTPMethods.sendPost("http://localhost:8080/rest/hello/dec", header);
        String resp = HTTPMethods.sendGet("http://localhost:8080/rest/hello/receive");
        System.out.println("GET RESULT 2");
        System.out.println(resp);
        byte[] key_bytes = resp.getBytes();

        SecretKey fkey = new SecretKeySpec(key_bytes,"AES");
        CipherEbook.decipher_file_1key();


    }
}
