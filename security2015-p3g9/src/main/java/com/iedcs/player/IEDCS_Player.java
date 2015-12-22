package com.iedcs.player;

/**
 * Created by Andre on 07-11-2015.
 */
import asg.cliche.Command;
import asg.cliche.ShellFactory;

import java.io.*;

import com.google.gson.Gson;
import com.iedcs.CitizenCard.Cartao;
import com.iedcs.CitizenCard.Citizen;
import com.iedcs.persistence.CatalogEntity;
import com.iedcs.persistence.UsercredEntity;
import com.iedcs.security.*;
import com.iedcs.web.HTTPMethods;
/*import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;*/

import org.hibernate.jpa.HibernatePersistenceProvider;
import pteidlib.*;


import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.URIParameter;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;


public class IEDCS_Player {
    private byte[] device_key;
    private PersistenceProvider persistenceProvider;
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private List<String> local_catalog;
    private String logged_user;



    public IEDCS_Player(){
        local_catalog = new ArrayList<String>();
        logged_user = new String();

    }

    public String getLogin(){
        return this.logged_user;
    }

    public void setLogin(String user){
        this.logged_user = user;
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

    @Command
    public String curuser(){
        return getLogin();
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
       appendToFile(getLogin(),ebook);

       //request server to cipher and send ebook
//       String get = HTTPMethods.sendGet("http://localhost:8080/rest/hello/download/"+dir);
       String local_directory = readFromFile("directory");
       local_directory = local_directory.replaceAll("\\s","");
        HTTPMethods.downloadFile("http://localhost:8080/rest/hello/download/"+dir,local_directory);
   }

   @Command // read Ebook
   public void read(String title) throws Exception{
      // String local_directory = "C:\\Users\\Andre\\Documents\\ebooks\\";
       //String local_directory = WorkingDirectories.getWorking_directory();
       String local_directory = readFromFile("directory");
       local_directory = local_directory.replaceAll("\\s","");
       System.out.println("directory read: "+local_directory);

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
            System.out.print("********* ");
            System.out.print(cat.getTitle());
            System.out.println(" *********");
        }
    }

    @Command //show downloaded books
    public void show() throws Exception{
        System.out.println("*******************************************************");
        System.out.println("******************** DOWNLOADED EBOOKS*****************");
        System.out.println("*******************************************************");
        String bought = readFromFile(getLogin());
        String[] tmp = bought.split(" ");

        for(String str : tmp){
            System.out.print("********* ");
            System.out.print(str);
            System.out.println(" **********");
        }
    }

     // register new user
    public static void register() throws Exception{
       // String resp = HTTPMethods.sendGet("http://localhost:8080/rest/hello/register"); //request register new user

        int ret = 0;
        Cartao cartao = new Cartao();
        try
        {
            // test.TestCVC();

            pteid.Init("");

            //test.TestChangeAddress();

            // Don't check the integrity of the ID, address and photo (!)
            pteid.SetSODChecking(false);

            int cardtype = pteid.GetCardType();
            switch (cardtype)
            {
                case pteid.CARD_TYPE_IAS07:
                    System.out.println("IAS 0.7 card\n");
                    break;
                case pteid.CARD_TYPE_IAS101:
                    System.out.println("IAS 1.0.1 card\n");
                    break;
                case pteid.CARD_TYPE_ERR:
                    System.out.println("Unable to get the card type\n");
                    break;
                default:
                    System.out.println("Unknown card type\n");
            }

            // Read ID Data
            PTEID_ID idData = pteid.GetID();

            }
        catch (PteidException ex)
        {
            ex.printStackTrace();
            //System.out.println(ex.getMessage());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            //System.out.println(ex.getMessage());
        }

        String cc_num = pteid.GetID().numBI;
        PTEID_Certif[] certs =  pteid.GetCertificates();
        PTEID_Certif citizen_cc = certs[0];
        byte[] cert_byte = citizen_cc.certif;
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(cert_byte);
        X509Certificate cert_citizencc = (X509Certificate)certFactory.generateCertificate(in);
        PublicKey citizen_pub_key = cert_citizencc.getPublicKey();
        String user_name = pteid.GetID().firstname+" "+pteid.GetID().name;

        String pub_key_user = new String(Base64.getEncoder().encode(citizen_pub_key.getEncoded()));
        Citizen citizen = new Citizen();
        citizen.setId(cc_num);
        citizen.setName(user_name);
        citizen.setUser_key("sim");
        citizen.setPass("debug2");
        citizen.setPublic_key(pub_key_user);
        Gson gson = new Gson();

        String json = gson.toJson(citizen);

        String cert_cc_str = new String( Base64.getEncoder().encode(cert_byte));
        HTTPMethods.sendPost("http://localhost:8080/rest/hello/register", json); //request register new user
        HTTPMethods.sendPost("http://localhost:8080/rest/hello/auth", cert_cc_str);

        writeToFile(user_name,""); //initialize user's bought books


    }

    @Command    // configs work directory
    public void setdirectory(String dir) throws Exception{
        WorkingDirectories.setWorkDirectory(dir);
        writeToFile("directory",dir);
    }

    public static String login() throws Exception{
        int ret = 0;
        Cartao cartao = new Cartao();
        try
        {
            // test.TestCVC();

            pteid.Init("");

            //test.TestChangeAddress();

            // Don't check the integrity of the ID, address and photo (!)
            pteid.SetSODChecking(false);

            int cardtype = pteid.GetCardType();
            switch (cardtype)
            {
                case pteid.CARD_TYPE_IAS07:
                    System.out.println("IAS 0.7 card\n");
                    break;
                case pteid.CARD_TYPE_IAS101:
                    System.out.println("IAS 1.0.1 card\n");
                    break;
                case pteid.CARD_TYPE_ERR:
                    System.out.println("Unable to get the card type\n");
                    break;
                default:
                    System.out.println("Unknown card type\n");
            }

            // Read ID Data
            PTEID_ID idData = pteid.GetID();

        }
        catch (PteidException ex)
        {
            ex.printStackTrace();
            //System.out.println(ex.getMessage());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            //System.out.println(ex.getMessage());
        }

        PTEID_Certif cert = pteid.GetCertificates()[0];
        byte[] cert_byte = cert.certif;
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(cert_byte);
        X509Certificate cert_citizencc = (X509Certificate)certFactory.generateCertificate(in);

        PublicKey pub = cert_citizencc.getPublicKey();
        byte[] pub_encoded = Base64.getEncoder().encode(pub.getEncoded());
        String pub_key_str = new String(pub_encoded);




        String res =HTTPMethods.sendPost("http://localhost:8080/rest/hello/login",pub_key_str);
        String status;
        if(res.equals("failed")) {
            System.out.println("---");
            status = "-1";
        }
        else{
            System.out.println("login succesful!");

            String cert_cc_str = new String( Base64.getEncoder().encode(cert_byte));
            HTTPMethods.sendPost("http://localhost:8080/rest/hello/auth", cert_cc_str);
            status = res;

        }

        return status;
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

    public static void writeToFile(String file, String str){
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file+".txt"), "utf-8"))) {
            writer.write(str);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void appendToFile(String file, String str){

        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file+".txt", true)))) {
            out.println(str);

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFromFile(String file){

        BufferedReader br = null;
        String out ="";

        try {

            String sCurrentLine;

            br = new BufferedReader(new FileReader(file+".txt"));

            while ((sCurrentLine = br.readLine()) != null) {
              //  System.out.println(sCurrentLine);
                out += sCurrentLine;
                out += " ";

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
       // System.out.println("out: "+out);
        return out;
    }



    public static void main(String[] args) throws Exception{
        String player_serial = "banan"; //hardcoded player key
       // final String SELECT_QUERY = "from CatalogEntity  where id = :id" ;
        String logged="";

        System.out.println("choose working directory");
        Scanner sc = new Scanner(System.in);
        String dir = sc.nextLine();
        if(dir.equals("debug")) {
            WorkingDirectories.setWorkDirectory("C:\\Users\\Andre\\Documents\\ebooks\\");
            writeToFile("directory","C:\\Users\\Andre\\Documents\\ebooks\\");
        }
        else {
            WorkingDirectories.setWorkDirectory(dir);
            writeToFile("directory",dir);
        }
       // validate(); //checks if client's player key is registred on server's player key database
        logged = login();

        if(logged.equals("-1")) {
            System.out.println("login failed");
            System.out.println("register new user (y/n) ?");
            String sel = sc.nextLine();
            if(sel.equals("y")) {
                register();
                logged = login();
            }
            else {
                System.out.println("exiting...");
                System.exit(-1);
            }
        }
        else{
            validate();

        }
        System.out.println("*******************************************************");
        System.out.println("***************WELCOME TO IEDCS PLAYER*****************");
        System.out.println("*******************************************************");

        IEDCS_Player player = new IEDCS_Player();
        player.setLogin(logged);
        ShellFactory.createConsoleShell("IEDCS@"+logged, "", player)
                .commandLoop();
        //int id = 1;


      //  List<CatalogEntity> catalog = player.getEntityManager().createQuery(SELECT_QUERY, CatalogEntity.class).setParameter("id",id).getResultList();
      //  System.out.println(catalog.get(0).getTitle());


        player.getEntityManager().close();


    }
}
