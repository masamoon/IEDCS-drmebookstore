package com.iedcs.security;

import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import org.bouncycastle.*; 

public class Ciph {

    private KeyPair pair; 
    private PublicKey pub;
    private PrivateKey priv;

    public Ciph() throws Exception {
       KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
              SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
              keyGen.initialize(1024, random);
              
              this.pair = keyGen.generateKeyPair();
              this.priv = pair.getPrivate();
              this.pub = pair.getPublic();
    }

    public byte[] encryptEbook(File ebook) throws Exception {
        
        Cipher ciph = Cipher.getInstance("RSA/NONE/PKCS1PADDING"); 
        ciph.init(Cipher.ENCRYPT_MODE, pub);
        return ciph.doFinal(ebook.toString().getBytes());

    }

    public byte[] decryptEbook(byte[] cyphered) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1PADDING");
        cipher.init(Cipher.DECRYPT_MODE, priv);
        return cipher.doFinal(cyphered);
      
    }

    public String hashEbook(Ebook ebook) throws Exception {
        String cont = ebook.getContent();

        byte[] bytesOfMessage = cont.getBytes("UTF-8");

        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] thedigest = md.digest(bytesOfMessage);
        
        return thedigest.toString(); 
    }
}
