
package com.iedcs.security;

import java.security.SecureRandom;
import java.util.Random;

/**
 *
 * @author Andre
 */
public class Salt {

    private final Random r;

    public Salt() {
        r = new SecureRandom();
        byte[] salt = new byte[32];
        r.nextBytes(salt);
        /**
         * String encodedSalt = Base64.encodeBase64String(salt);
         */
    }
    
    public Random getSalt(){
        return r;
    }
}
