package com.iedcs.security;

import com.iedcs.persistence.UsercredEntity;
import org.hibernate.jpa.HibernatePersistenceProvider;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Andre on 09-11-2015.
 */
public class UserKeyFactory {

    public static SecretKey getKey() throws Exception{

        //only one (hardcoded) user on database
        PersistenceProvider persistenceProvider = new HibernatePersistenceProvider();
        EntityManagerFactory entityManagerFactory = persistenceProvider.createEntityManagerFactory("NewPersistenceUnit", new HashMap());
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        int id = 1;
        String SELECT_QUERY = "from UsercredEntity  where id = :id" ;
        List<UsercredEntity> user = entityManager.createQuery(SELECT_QUERY, UsercredEntity.class).setParameter("id",id).getResultList();
        String pre_user_key = user.get(0).getUserKey(); //fetch user's user key
        byte[] encoded_userkey = pre_user_key.getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        encoded_userkey = sha.digest(encoded_userkey);
        encoded_userkey = Arrays.copyOf(encoded_userkey, 32);
        SecretKey user_key = new SecretKeySpec(encoded_userkey,0,encoded_userkey.length,"AES");

        return user_key;
    }

    public static SecretKey getKeyUser(String username) throws Exception{
        PersistenceProvider persistenceProvider = new HibernatePersistenceProvider();
        EntityManagerFactory entityManagerFactory = persistenceProvider.createEntityManagerFactory("NewPersistenceUnit", new HashMap());
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        String SELECT_QUERY = "from UsercredEntity  where username = :username" ;
        List<UsercredEntity> user = entityManager.createQuery(SELECT_QUERY, UsercredEntity.class).setParameter("username",username).getResultList();
        String pre_user_key = user.get(0).getUserKey(); //fetch user's user key
        byte[] encoded_userkey = pre_user_key.getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        encoded_userkey = sha.digest(encoded_userkey);
        encoded_userkey = Arrays.copyOf(encoded_userkey, 32);
        SecretKey user_key = new SecretKeySpec(encoded_userkey,0,encoded_userkey.length,"AES");

        return user_key;
    }
}
