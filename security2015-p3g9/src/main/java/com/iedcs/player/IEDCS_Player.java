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
import sun.security.pkcs11.SunPKCS11;
import sun.security.pkcs11.wrapper.CK_MECHANISM;
import sun.security.pkcs11.wrapper.PKCS11Constants;


import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.security.auth.callback.CallbackHandler;
import java.net.URLEncoder;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.text.Normalizer;
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



    @Command(description="displays current logged user's profile pic and username")
    public String curuser()throws Exception{
        ReadPic.showPic();
        return getLogin();
    }

    @Command // exit
    public void exit(){
        System.exit(0);
    }



   @Command(description="Downloads Ebook , first parameter is ebook title according to the catalog") // Download Ebook
   public void download(String ebook) throws Exception{

       if(!signhash(getLogin())){
           System.out.println("failed to authenticate user: failed to verify digital signature");
           System.exit(1);
       }

       String dir = HTTPMethods.sendPost("http://localhost:8080/rest/hello/fetchbook",ebook); //fetches filename of desired ebook


     //  String dir = catalog.get(0).getDirectory();
       //update local catalog
       local_catalog.add(ebook);
       appendToFile(getLogin(),ebook);

       //request server to cipher and send ebook
       String local_directory = readFromFile("directory");
       local_directory = local_directory.replaceAll("\\s","");
        HTTPMethods.downloadFile("http://localhost:8080/rest/hello/download/"+dir+"/"+URLEncoder.encode(getLogin(), "UTF-8"),local_directory);
   }

   @Command (description="reads Ebook , first parameter is ebook title according to the catalog")// read Ebook
   public void read(String title) throws Exception{
      // String local_directory = "C:\\Users\\Andre\\Documents\\ebooks\\";
       //String local_directory = WorkingDirectories.getWorking_directory();
       if(!signhash(getLogin())){
           System.out.println("failed to authenticate user: failed to verify digital signature");
           System.exit(1);
       }

       String local_directory = readFromFile("directory");
       local_directory = local_directory.replaceAll("\\s","");
       System.out.println("reading book: "+title+"from: "+local_directory);

       // get filename of selected ebook

       String ebook = HTTPMethods.sendPost("http://localhost:8080/rest/hello/fetchbook",title);
       File f = new File(local_directory+ebook+"h.aes");    // ciphered ebook with crypto header
       File fout = new File(local_directory+ebook+".aes");  // ciphered ebook without crypto header
       byte[] header = MakeHeader.readHeader_bytes(f);                                    // read header
       Base64.Encoder encoder = Base64.getEncoder();                                 // encode header with base64

       MakeHeader.removeHeader(f,fout);                                             //remove header from ciphered ebook

       SecretKey first_key =  KeyWrapper.unwrap_playerKey(header);     // decipher with player key

       byte[] first_key_encoded = encoder.encode(first_key.getEncoded());

       Gson gson = new Gson();
       String[] tosend = {getLogin(),new String(first_key_encoded)};
       String tosend_json = gson.toJson(tosend);
      // HTTPMethods.sendPost("http://localhost:8080/rest/hello/dec",new String( first_key_encoded));           //send header to recover key
       HTTPMethods.sendPost("http://localhost:8080/rest/hello/dec",tosend_json);           //send header to recover key
       String resp = HTTPMethods.sendGet("http://localhost:8080/rest/hello/receive");   // get recovered key

       // byte[] key_bytes = resp.getBytes();
     //  System.out.println("RESP");
     //  System.out.println(resp);
       Base64.Decoder decoder = Base64.getDecoder();        //get base64 decoder
       byte[] key_bytes = decoder.decode(resp);             // decode response from server decipher
     //  System.out.println("USER KEY UNWRAP");
     //  System.out.println(new String(key_bytes));
       SecretKey fkey = KeyWrapper.unwrap_deviceKey(key_bytes);  //decipher with the final key : device key

       // SecretKey fkey = new SecretKeySpec(key_bytes,"AES");
     //  System.out.println("RECOVERED KEY");
     //  System.out.println(new String(fkey.getEncoded()));
       CipherEbook.decipher_file_1key(local_directory + ebook,fkey);


       File fdeciph = new File(local_directory + ebook+".aes.dec");
       FileInputStream fstream = new FileInputStream(fdeciph);
       GUI.view(fstream);




   }

    @Command (description="shows avaliable ebooks on the IEDCS catalog")//show catalog
    public void catalog() throws Exception{
           /* final String SELECT_QUERY = "from CatalogEntity";
            IEDCS_Player player = new IEDCS_Player();
            List<CatalogEntity> catalog = player.getEntityManager().createQuery(SELECT_QUERY, CatalogEntity.class).getResultList();*/
        String catalog_str = HTTPMethods.sendGet("http://localhost:8080/rest/hello/catalog");
        Gson gson = new Gson();
        CatalogEntity[] catalog = gson.fromJson(catalog_str, CatalogEntity[].class);

        System.out.println("*******************************************************");
        System.out.println("***********AVALIABLE CATALOG ON IEDCS SERVER***********");
        System.out.println("*******************************************************");
        for(CatalogEntity cat : catalog){
           System.out.printf("********* %-20s *********\n",cat.getTitle());
        }
    }

    @Command(description="shows current logged in user's downloaded ebooks")//show downloaded books
    public void show() throws Exception{
        System.out.println("*******************************************************");
        System.out.println("******************** DOWNLOADED EBOOKS*****************");
        System.out.println("*******************************************************");
        stripDuplicatesFromFile(getLogin()+".txt");
        String bought = readFromFile(getLogin());
        String[] tmp = bought.split(" ");

        for(String str : tmp){
            System.out.printf("********* %-20s *********\n",str);

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


        user_name = Normalizer.normalize(user_name, Normalizer.Form.NFD);
        String username_norm = user_name.replaceAll("[^\\x00-\\x7F]", "");

        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[32];
        random.nextBytes(bytes);

        String user_key = Base64.getEncoder().encodeToString(bytes);

        String pub_key_user = new String(Base64.getEncoder().encode(citizen_pub_key.getEncoded()));
        Citizen citizen = new Citizen();
        citizen.setId(cc_num);
        citizen.setName(username_norm);
        //citizen.setUser_key("sim");
        citizen.setUser_key(user_key);
        citizen.setPass("debug2");
        citizen.setPublic_key(pub_key_user);
        Gson gson = new Gson();

        String json = gson.toJson(citizen);

        String cert_cc_str = new String( Base64.getEncoder().encode(cert_byte));
        HTTPMethods.sendPost("http://localhost:8080/rest/hello/register", json); //request register new user
        HTTPMethods.sendPost("http://localhost:8080/rest/hello/auth", cert_cc_str);

        writeToFile(user_name,""); //initialize user's bought books


    }

    @Command(description="changes active work directory")    // configs work directory
    public void setdirectory(String dir) throws Exception{
        WorkingDirectories.setWorkDirectory(dir);
        writeToFile("directory",dir);
    }


    public static boolean signhash(String login) throws Exception{

        byte[] out = sign(login);
        byte[] tosend = Base64.getEncoder().encode(out);
        System.out.println("verifying digital signature...");
        Gson gson = new Gson();
        String[] param = new String[2];
        param[0]= login;
        param[1]= new String(tosend);
        String param_json = gson.toJson(param);
       // System.out.println("sent json"+param_json);
        String res = HTTPMethods.sendPost("http://localhost:8080/rest/hello/authprivate", param_json);
       // System.out.println("res:"+res);
        if(res.equals("true")) {
            System.out.println("verified!");
            return true;
        }
        else
            return false;
    }




    public static String login() throws Exception{
        int ret = 0;
        System.out.println("logging in...");
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
            System.out.println("login successful!");

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

        //File fpk = new File("C:\\Users\\Andre\\Documents\\ebooks\\cpublickey.key"); // save public key on file
        File fpk = new File("cpublickey.key"); // save public key on file
       /* if(fpk.getParentFile() != null){
            fpk.mkdirs();
        }
        fpk.createNewFile();*/

        FileOutputStream fout = new FileOutputStream(fpk);
        fout.write(pk);
        fout.close();


       // ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("C:\\Users\\Andre\\Documents\\ebooks\\cpublickey.key")); //retrieve pubic key object from file
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("cpublickey.key")); //retrieve pubic key object from file

        PublicKey publicKey = (PublicKey) inputStream.readObject();

        Base64.Encoder encoder = Base64.getEncoder();

        SecretKey player_key = PlayerKeyFactory.getKey();
        byte[] player_key_bytes = player_key.getEncoded();
        byte[] player_key_encoded = encoder.encode(player_key_bytes);

       // byte[] token = EncryptionUtil.encrypt("banan",publicKey);
        byte[] token = EncryptionUtil.encrypt(new String(player_key_encoded),publicKey);

        byte[] encoded_token = encoder.encode(token);
        String res = HTTPMethods.sendPost("http://localhost:8080/rest/hello/validate",new String(encoded_token));
        System.out.println(res);
    }

    public static byte[] sign(String hash){
        try {
            String osName = System.getProperty("os.name");
            String pkcs11config = "name=GemPC" + "\n"
                    + "library=C:/WINDOWS/system32/pteidpkcs11.dll";

            byte[] pkcs11configBytes = pkcs11config.getBytes();
            ByteArrayInputStream configStream = new ByteArrayInputStream(pkcs11configBytes);

            Provider p = new SunPKCS11(configStream);
            Security.addProvider(p);
            CallbackHandler cmdLineHdlr = new com.sun.security.auth.callback.TextCallbackHandler();
            KeyStore.Builder builder = KeyStore.Builder.newInstance("PKCS11", p,
                    new KeyStore.CallbackHandlerProtection(cmdLineHdlr));
            KeyStore ks = builder.getKeyStore();
            String assinaturaCertifLabel = "CITIZEN AUTHENTICATION CERTIFICATE";
           // Certificate[] chain = ks.getCertificateChain(assinaturaCertifLabel);
            Key key = ks.getKey(assinaturaCertifLabel, null);
           PublicKey pub_key = ks.getCertificate(assinaturaCertifLabel).getPublicKey();
           // System.out.println("public key: "+ new String(pub_key.getEncoded()));


            //
           /* System.out.println(pteid.GetCertificates()[0].certifLabel);
           byte[] cert_byte = pteid.GetCertificates()[0].certif;
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(cert_byte);
            X509Certificate cert_citizencc = (X509Certificate)certFactory.generateCertificate(in);
            PublicKey citizen_pub_key = cert_citizencc.getPublicKey();*/
            //

            CK_MECHANISM mechanism = new CK_MECHANISM();
            mechanism.mechanism = PKCS11Constants.CKM_RSA_PKCS;
            mechanism.pParameter = null;



            Signature sig = Signature.getInstance("SHA1withRSA",p);
            sig.initSign((PrivateKey)key) ;
            sig.update(hash.getBytes());
            byte[] signedHash = sig.sign();

           // System.out.println("to sign: "+hash);
           // System.out.println("signed: "+ new String(signedHash));

           /* Signature verifier = Signature.getInstance("SHA1withRSA");
            verifier.initVerify(pub_key);
            verifier.update(hash.getBytes());
            boolean ok = verifier.verify(signedHash);
            System.out.println("ok: "+ok);*/

            return signedHash;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

    public void stripDuplicatesFromFile(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        Set<String> lines = new HashSet<String>(10000);
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        reader.close();
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (String unique : lines) {
            writer.write(unique);
            writer.newLine();
        }
        writer.close();
    }



    public static void main(String[] args) throws Exception{
        String player_serial = "banan"; //hardcoded player key

        String logged="";

        System.out.println("choose working directory");
        System.out.print(">");
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

        logged = login();

        if(logged.equals("-1")) {
            System.out.println("login failed");
            System.out.println("register new user (y/n) ?");
            String sel = sc.nextLine();
            if(sel.equals("y")) {
                register();
                logged = login();
                validate(); //checks if client's player key is registred on server's player key database
                if(!signhash(logged)){
                    System.out.println("failed to authenticate user: failed to verify digital signature");
                    System.exit(1);
                }
            }
            else {
                System.out.println("exiting...");
                System.exit(-1);
            }
        }
        else{
            validate();//checks if client's player key is registred on server's player key database
            if(!signhash(logged)){
                System.out.println("failed to authenticate user: failed to verify digital signature");
                System.exit(1);
            }

        }
        System.out.println("*******************************************************");
        System.out.println("***************WELCOME TO IEDCS PLAYER*****************");
        System.out.println("*******************************************************");

        IEDCS_Player player = new IEDCS_Player();
        player.setLogin(logged);
        ShellFactory.createConsoleShell("IEDCS@"+logged, "", player)
                .commandLoop();



        player.getEntityManager().close();


    }
}
