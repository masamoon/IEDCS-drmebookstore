package com.iedcs.security;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by Andre on 03-11-2015.
 */
public class PlayerKeyFactory  {






    public static SecretKey getKey()throws Exception{
        String key = "FRUTFRUTFRUTFRUTFRUTFRUTFRUTFRUTRUTFRUT";
        byte[] encodedkey = key.getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        encodedkey = sha.digest(encodedkey);
        encodedkey = Arrays.copyOf(encodedkey, 32); // use only first 256 bit
        SecretKey originalKey = new SecretKeySpec(encodedkey, 0, encodedkey.length, "AES");
        return originalKey;
    }


}
