package com.iedcs.persistence;

import org.hibernate.jpa.HibernatePersistenceProvider;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Andre on 07-11-2015.
 */
public class Main {
    public static final String SELECT_QUERY = "from CatalogEntity  where id = :id" ;

    public static void main(String[] args){
        int id = 1;


        PersistenceProvider persistenceProvider = new HibernatePersistenceProvider();

        EntityManagerFactory entityManagerFactory = persistenceProvider.createEntityManagerFactory("NewPersistenceUnit", new HashMap());
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        List<CatalogEntity> catalog = entityManager.createQuery(SELECT_QUERY,CatalogEntity.class).setParameter("id",id).getResultList();
        System.out.println(catalog.get(0).getTitle());
        entityManager.close();




    }


}
