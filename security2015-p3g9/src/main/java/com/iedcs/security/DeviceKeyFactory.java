package com.iedcs.security;

import org.apache.commons.lang.SystemUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;

/**
 * Created by Andre on 02-11-2015.
 */
public class DeviceKeyFactory {



    public static byte[] getDevice()throws Exception{
        String serial = DiskUtils.getSerialNumber("C");
        String mac = Mac.getMac();
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String text = serial + mac;
        md.update(text.getBytes("UTF-8")); // Change this to "UTF-16" if needed
        byte[] device_hash = md.digest();
        SecretKey originalKey = new SecretKeySpec(device_hash, 0, device_hash.length, "AES");

        return device_hash;
    }

    public static SecretKey getDeviceKey() throws Exception{
        String serial ="";
        if(System.getProperty("os.name").startsWith("Windows")){
            serial = DiskUtils.getSerialNumber("C");
        }
        else{
            serial = "unixdefaultserial";
        }
       // String serial = DiskUtils.getSerialNumber("C");
        String mac = Mac.getMac();
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String text = serial + mac;
        md.update(text.getBytes("UTF-8")); // Change this to "UTF-16" if needed
        byte[] device_hash = md.digest();
        SecretKey originalKey = new SecretKeySpec(device_hash, 0, device_hash.length, "AES");

        return originalKey;
    }

}
