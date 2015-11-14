package com.iedcs.player;

/**
 * Created by Andre on 07-11-2015.
 */
import asg.cliche.Command;
import asg.cliche.ShellFactory;

import java.io.File;
import java.io.IOException;

import com.iedcs.persistence.CatalogEntity;
import com.iedcs.security.CipherEbook;
import com.iedcs.security.DeviceKeyFactory;
import com.iedcs.security.Ebook;
import com.iedcs.security.SQLqueries;
import com.iedcs.web.HTTPMethods;
/*import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;*/

import org.hibernate.jpa.HibernatePersistenceProvider;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class IEDCS_Player {
    private byte[] device_key;
    private PersistenceProvider persistenceProvider;
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private List<Ebook> local_catalog;




    public IEDCS_Player(){
        local_catalog = new ArrayList<Ebook>();

    }

    public static EntityManager getEntityManager(){
        PersistenceProvider persistenceProvider = new HibernatePersistenceProvider();

        EntityManagerFactory entityManagerFactory = persistenceProvider.createEntityManagerFactory("NewPersistenceUnit", new HashMap());
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        return entityManager;
    }

    @Command
    public String hello() {
        return "Hello, World!";
    }

    @Command
    public int add(int a, int b) {
        return a + b;
    }

    @Command // exit
    public void exit(){
        System.exit(0);
    }

    @Command //init player
    public void init() throws Exception{
         device_key = DeviceKeyFactory.getDevice();

    }

    @Command //Buy ebook
    public void buy (int id)throws Exception{
        String url = "http://localhost:8080/encrypt/"+id;
        HTTPMethods.sendGet(url);                           //send GET request to buy and cipher book
    }


   @Command // Download Ebook
   public void download(int id) throws Exception{
       String SELECT_QUERY = SQLqueries.select("CatalogEntity", "id", Integer.toString(id));
       //List<CatalogEntity> catalog = getEntityManager().createQuery(SELECT_QUERY, CatalogEntity.class).setParameter("id",Integer.toString(id)).getResultList();
       String url = "http://localhost:8080/rest/hello/download/"+id;
       String response = HTTPMethods.sendGet(url);

   }

   @Command // read Ebook
   public void read(int id) throws Exception{

       String url = "http://localhost:8080/rest/hello/"+id;

      /* CipherEbook.decipher_file1("C:\\Users\\Andre\\Documents\\ebooks\\meinkampf.epub");

       HTTPMethods.uploadFile(new File("C:\\Users\\Andre\\Documents\\ebooks\\meinkampf.epub.aes"),"http://localhost:8080/rest/hello/decrypt2");

       CipherEbook.decipher_file3("C:\\Users\\Andre\\Documents\\ebooks\\meinkampf.epub");*/




   }

    @Command //show catalog
    public void show() throws Exception{

    }



    public static void main(String[] args) throws IOException {
        String player_serial = "banan"; //hardcoded player key
       // final String SELECT_QUERY = "from CatalogEntity  where id = :id" ;

        IEDCS_Player player = new IEDCS_Player();
        ShellFactory.createConsoleShell("IEDCS", "", player)
                .commandLoop();
        //int id = 1;


      //  List<CatalogEntity> catalog = player.getEntityManager().createQuery(SELECT_QUERY, CatalogEntity.class).setParameter("id",id).getResultList();
      //  System.out.println(catalog.get(0).getTitle());


        player.getEntityManager().close();


    }
}
