package com.iedcs.player;

/**
 * Created by Andre on 07-11-2015.
 */
import asg.cliche.Command;
import asg.cliche.ShellFactory;

import java.io.*;

import com.iedcs.persistence.CatalogEntity;
import com.iedcs.security.*;
import com.iedcs.web.HTTPMethods;
/*import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;*/

import org.hibernate.jpa.HibernatePersistenceProvider;


import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;


public class IEDCS_Player {
    private byte[] device_key;
    private PersistenceProvider persistenceProvider;
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private List<String> local_catalog;




    public IEDCS_Player(){
        local_catalog = new ArrayList<String>();

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

   /* @Command //init player
    public void init() throws Exception{
         device_key = DeviceKeyFactory.getDevice();

    }*/

    /*@Command //Buy ebook
    public void buy (int id)throws Exception{
        String url = "http://localhost:8080/encrypt/"+id;
        HTTPMethods.sendGet(url);                           //send GET request to buy and cipher book
    }*/


   @Command(description="Downloads Ebook , first parameter is ebook title according to the catalog") // Download Ebook
   public void download(String ebook) throws Exception{
       PersistenceProvider persistenceProvider = new HibernatePersistenceProvider();

       EntityManagerFactory entityManagerFactory = persistenceProvider.createEntityManagerFactory("NewPersistenceUnit", new HashMap());
       EntityManager entityManager = entityManagerFactory.createEntityManager();

       // get filename of selected ebook
       String SELECT_QUERY = "from CatalogEntity where title = :title";
       List<CatalogEntity> catalog = getEntityManager().createQuery(SELECT_QUERY, CatalogEntity.class).setParameter("title",ebook).getResultList();
       SecretKey user_key = UserKeyFactory.getKey();
       SecretKey device_key = DeviceKeyFactory.getDeviceKey();
       SecretKey player_key = PlayerKeyFactory.getKey();
       SecretKey file_key = FileKeyFactory.getKey();

       String dir = catalog.get(0).getDirectory();
       //update local catalog
       local_catalog.add(ebook);

       //request server to cipher and send ebook
       String get = HTTPMethods.sendGet("http://localhost:8080/rest/hello/"+dir);

   }

   @Command // read Ebook
   public void read(String title) throws Exception{
      // String local_directory = "C:\\Users\\Andre\\Documents\\ebooks\\";
       String local_directory = WorkingDirectories.getWorking_directory();

       // get filename of selected ebook
       String SELECT_QUERY = "from CatalogEntity where title = :title";
       List<CatalogEntity> catalog = getEntityManager().createQuery(SELECT_QUERY, CatalogEntity.class).setParameter("title",title).getResultList();
       String ebook = catalog.get(0).getDirectory();
       File f = new File(local_directory+ebook+"h.aes");    // ciphered ebook with crypto header
       File fout = new File(local_directory+ebook+".aes");  // ciphered ebook without crypto header
       byte[] header = MakeHeader.readHeader_bytes(f);                                    // read header
       Base64.Encoder encoder = Base64.getEncoder();                                 // encode header with base64

       MakeHeader.removeHeader(f,fout);                                             //remove header from ciphered ebook

       SecretKey first_key =  KeyWrapper.unwrap_playerKey(header);     // decipher with player key

       byte[] first_key_encoded = encoder.encode(first_key.getEncoded());
       HTTPMethods.sendPost("http://localhost:8080/rest/hello/dec",new String( first_key_encoded));           //send header to recover key
       String resp = HTTPMethods.sendGet("http://localhost:8080/rest/hello/receive");   // get recovered key

       // byte[] key_bytes = resp.getBytes();
       System.out.println("RESP");
       System.out.println(resp);
       Base64.Decoder decoder = Base64.getDecoder();        //get base64 decoder
       byte[] key_bytes = decoder.decode(resp);             // decode response from server decipher
       System.out.println("USER KEY UNWRAP");
       System.out.println(new String(key_bytes));
       SecretKey fkey = KeyWrapper.unwrap_deviceKey(key_bytes);  //decipher with the final key : device key

       // SecretKey fkey = new SecretKeySpec(key_bytes,"AES");
       System.out.println("RECOVERED KEY");
       System.out.println(new String(fkey.getEncoded()));
       CipherEbook.decipher_file_1key(local_directory + ebook,fkey);


       File fdeciph = new File(local_directory + ebook+".aes.dec");
       FileInputStream fstream = new FileInputStream(fdeciph);
       GUI.view(fstream);




   }

    @Command //show catalog
    public void catalog() throws Exception{
            final String SELECT_QUERY = "from CatalogEntity";
            IEDCS_Player player = new IEDCS_Player();
            List<CatalogEntity> catalog = player.getEntityManager().createQuery(SELECT_QUERY, CatalogEntity.class).getResultList();
        System.out.println("*******************************************************");
        System.out.println("***********AVALIABLE CATALOG ON IEDCS SERVER***********");
        System.out.println("*******************************************************");
        for(CatalogEntity cat : catalog){
                System.out.println(cat.getTitle());
            }
    }

    @Command //show downloaded books
    public void show() throws Exception{
        System.out.println("*******************************************************");
        System.out.println("******************** DOWNLOADED EBOOKS*****************");
        System.out.println("*******************************************************");
        for(String str : local_catalog){
            System.out.println(str);
        }
    }

    @Command    // configs work directory
    public void setdirectory(String dir) throws Exception{
        WorkingDirectories.setWorkDirectory(dir);
    }

   // @Command // validate client using asymmetric cryptography
    public static void validate() throws Exception{
        String resp = HTTPMethods.sendGet("http://localhost:8080/rest/hello/sendPK"); //get public key from server
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] pk = decoder.decode(resp);

        File fpk = new File("C:\\Users\\Andre\\Documents\\ebooks\\cpublickey.key"); // save public key on file
       /* if(fpk.getParentFile() != null){
            fpk.mkdirs();
        }
        fpk.createNewFile();*/

        FileOutputStream fout = new FileOutputStream(fpk);
        fout.write(pk);
        fout.close();


        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("C:\\Users\\Andre\\Documents\\ebooks\\cpublickey.key")); //retrieve pubic key object from file
        PublicKey publicKey = (PublicKey) inputStream.readObject();

        Base64.Encoder encoder = Base64.getEncoder();

        SecretKey player_key = PlayerKeyFactory.getKey();
        byte[] player_key_bytes = player_key.getEncoded();
        byte[] player_key_encoded = encoder.encode(player_key_bytes);

       // byte[] token = EncryptionUtil.encrypt("banan",publicKey);
        byte[] token = EncryptionUtil.encrypt(new String(player_key_encoded),publicKey);

        byte[] encoded_token = encoder.encode(token);
        HTTPMethods.sendPost("http://localhost:8080/rest/hello/validate",new String(encoded_token));

    }



    public static void main(String[] args) throws Exception{
        String player_serial = "banan"; //hardcoded player key
       // final String SELECT_QUERY = "from CatalogEntity  where id = :id" ;

        System.out.println("choose working directory");
        Scanner sc = new Scanner(System.in);
        WorkingDirectories.setWorkDirectory(sc.nextLine());
        validate(); //checks if client's player key is registred on server's player key database

        System.out.println("*******************************************************");
        System.out.println("***************WELCOME TO IEDCS PLAYER*****************");
        System.out.println("*******************************************************");

        IEDCS_Player player = new IEDCS_Player();

        ShellFactory.createConsoleShell("IEDCS", "", player)
                .commandLoop();
        //int id = 1;


      //  List<CatalogEntity> catalog = player.getEntityManager().createQuery(SELECT_QUERY, CatalogEntity.class).setParameter("id",id).getResultList();
      //  System.out.println(catalog.get(0).getTitle());


        player.getEntityManager().close();


    }
}
