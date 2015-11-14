package com.iedcs.security;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Created by Andre on 10-11-2015.
 */
public class FileKeyFactory {

    public static SecretKey getKey()throws Exception{

        SecretKey player_key = PlayerKeyFactory.getKey();
        SecretKey user_key = UserKeyFactory.getKey();
        SecretKey device_key = DeviceKeyFactory.getDeviceKey();

        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        byte[] encoded_keys = sha.digest(player_key.getEncoded());
        byte[] encoded_keys2 = sha.digest(encoded_keys);
        byte[] encoded_keys3 = sha.digest(encoded_keys2);

        encoded_keys3 = Arrays.copyOf(encoded_keys3, 32);
        SecretKey file_key = new SecretKeySpec(encoded_keys3,0,encoded_keys3.length,"AES");

        return file_key;

    }

    public static byte[] wrappedKey() throws Exception{

        SecretKey player_key = PlayerKeyFactory.getKey();
        SecretKey user_key = UserKeyFactory.getKey();
        SecretKey device_key = DeviceKeyFactory.getDeviceKey();

        byte[] first_wrap = KeyWrapper.wrapKey(getKey(),device_key);

        return first_wrap;
    }
}
