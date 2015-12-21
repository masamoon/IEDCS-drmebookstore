package org.iedcs.api;

import javax.crypto.SecretKey;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.spi.PersistenceProvider;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.iedcs.CitizenCard.Cartao;
import com.iedcs.CitizenCard.Citizen;
import com.iedcs.persistence.PlayerkeytableEntity;
import com.iedcs.persistence.UsercredEntity;
import com.iedcs.security.*;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.mrpdaemon.sec.encfs.*;
import pteidlib.PTEID_ID;
import pteidlib.PteidException;
import pteidlib.pteid;

import java.io.*;
import java.net.URLDecoder;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;


@Path("/hello")
public class HelloWorldService {
    //String local_directory = "C:\\Users\\Andre\\Documents\\ebooks\\"; //static directory
    String local_directory = WorkingDirectories.getLocalDirectory(); //
   // String local_directory = WorkingDirectories.getWorking_directory();
    //String local_directory = "ebooks/";


    @GET
    @Path("/download/{param}")
    public Response getMessage(@PathParam("param") String message)throws Exception {
        //String local_directory = "C:\\Users\\Andre\\Documents\\ebooks\\";
       // String local_directory = "\\ebooks\\";
        System.out.println("ENTERING GET REQUEST");
        String output = "Downloading: " + message;
        CipherEbook.cipher_file_1key(local_directory+message, FileKeyFactory.getKey()); // cipher ebook with file key
        System.out.println("WRAPPING FILE KEY");
        byte[] wrappedKey = KeyWrapper.wrapKey_3(DeviceKeyFactory.getDeviceKey(), UserKeyFactory.getKey(), PlayerKeyFactory.getKey(),FileKeyFactory.getKey()); // cipher file key with device,user and player key
        MakeHeader.make(new File(local_directory+message+".aes"),wrappedKey,new File(local_directory+message+"h.aes")); //add crypto header
        System.out.println("ALL DONE");
        return Response.status(200).entity(output).build();
    }

    @POST
    @Path("/dec/")
    public Response sendCryptoinfo(String message)throws Exception {
        //String local_directory = "C:\\Users\\Andre\\Documents\\ebooks\\";


       /* System.out.println("WRAPPED KEY ON POST");
        System.out.println(message);
        System.out.println("WRAPPED KEY DECODED ON POST");*/


        Base64.Decoder decoder = Base64.getDecoder();   //get decoder
        byte[] message_decoded = decoder.decode(message.getBytes()); //decode POST parameter from Base64

       // SecretKey unwrappedKey = KeyWrapper.unwrapKey_3(DeviceKeyFactory.getDeviceKey(),UserKeyFactory.getKey(),PlayerKeyFactory.getKey(),message_decoded);
       SecretKey unwrappedKey = KeyWrapper.unwrap_userKey(message_decoded);
        System.out.println("UNWRAPPED KEY ON POST");
        System.out.println(new String(unwrappedKey.getEncoded()));

        //return Response.status(200).entity(output).build();

        String output = "decoding on server...";

        File fout = new File(local_directory+"file_key");
        FileOutputStream out = new FileOutputStream(fout);
        out.write(unwrappedKey.getEncoded());
        out.close();

        return Response.status(200).entity(output).build();
    }

    @GET
    @Path("/receive")
    public Response receiveCryptoinfo()throws Exception{
        //String local_directory = "C:\\Users\\Andre\\Documents\\ebooks\\";
        byte[] buffer = new  byte[32];
        File fout = new File(local_directory+"file_key");
        FileInputStream fin = new FileInputStream(fout);
        int read;
        fin.read(buffer);
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encoded_buffer = encoder.encode(buffer);

        return Response.status(200).entity(encoded_buffer).build();
    }

    @GET
    @Path("/sendPK")    //sends public key to client
    public Response sendPK()throws Exception{
        if(!EncryptionUtil.areKeysPresent()){
            EncryptionUtil.generateKey();
        }

        FileInputStream fin = new FileInputStream(new File(EncryptionUtil.PUBLIC_KEY_FILE));
        byte[] pk = new byte[419];
        System.out.println("decoded pkey");

        fin.read(pk);
        System.out.println(new String(pk));
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encoded_pk = encoder.encode(pk);
        return Response.status(200).entity(encoded_pk).build();
    }

    @POST
    @Path("/validate")      //checks if client's player key is registred on the database
    public Response validate(String message) throws Exception{
        ObjectInputStream inputStream = null;
        inputStream = new ObjectInputStream(new FileInputStream(EncryptionUtil.PRIVATE_KEY_FILE));
        PrivateKey privateKey = (PrivateKey) inputStream.readObject();
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] message_decoded = decoder.decode(message);
        String token = EncryptionUtil.decrypt(message_decoded,privateKey);

        //hibernate objects
        PersistenceProvider persistenceProvider = new HibernatePersistenceProvider();
        EntityManagerFactory entityManagerFactory = persistenceProvider.createEntityManagerFactory("NewPersistenceUnit", new HashMap());
        EntityManager entityManager = entityManagerFactory.createEntityManager();


        String SELECT_QUERY = "from PlayerkeytableEntity where playerkey = :playerkey";
       // String SELECT_QUERY = "from PlayerkeytableEntity ";
        List<PlayerkeytableEntity> PlayerKey = entityManager.createQuery(SELECT_QUERY, PlayerkeytableEntity.class).setParameter("playerkey",token).getResultList();

        String result ="";

        if(PlayerKey.size()>0){
            result = "Client validated";
        }
        else{
            result = "no match found";
            return Response.status(401).entity(result).build(); // the player key provided by the client was not found on the DB
        }

        return Response.status(200).entity(result).build();
    }

    @POST
    @Path("/register")
    public Response register(String message) throws Exception{

        String result = "";


        //hibernate objects
        PersistenceProvider persistenceProvider = new HibernatePersistenceProvider();
        EntityManagerFactory entityManagerFactory = persistenceProvider.createEntityManagerFactory("NewPersistenceUnit", new HashMap());
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        Gson gson = new Gson();
        Citizen citizen = gson.fromJson(message,Citizen.class);



        UsercredEntity user = new UsercredEntity();
        user.setId(Integer.parseInt(citizen.getId()));
        user.setUsername(citizen.getName());
        user.setPasshash(citizen.getPass());
        user.setUserKey(citizen.getUser_key());
        user.setPublicKey(citizen.getPublic_key());

        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        entityManager.persist(user);
        tx.commit();



        result = "Welcome to IEDCS "+ citizen.getName();
        return Response.status(200).entity(result).build();

    }

    @POST
    @Path("/login")
    public Response login(String message) throws Exception{

        //hibernate objects
        PersistenceProvider persistenceProvider = new HibernatePersistenceProvider();
        EntityManagerFactory entityManagerFactory = persistenceProvider.createEntityManagerFactory("NewPersistenceUnit", new HashMap());
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        String pubkey= new String(message);
        System.out.println("pubkey = "+pubkey);
        String SELECT_QUERY = "from UsercredEntity where publicKey = :pubkey";
        // String SELECT_QUERY = "from PlayerkeytableEntity ";
        List<UsercredEntity> user = entityManager.createQuery(SELECT_QUERY, UsercredEntity.class).setParameter("pubkey",pubkey).getResultList();

        String res = "";
        if(user.size()>0)
            res = user.get(0).getUsername();
        else
            res = "failed";
        return Response.status(200).entity(res).build();
    }

    @POST
    @Path("/auth")
    public Response authenticate(String message) throws Exception{

        byte[] cert_byte = Base64.getDecoder().decode(message.getBytes());
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(cert_byte);
        X509Certificate cert_citizencc = (X509Certificate)certFactory.generateCertificate(in);

        certFactory = CertificateFactory.getInstance("X.509");

        String encrypt_dir = "C:\\Users\\Andre\\Documents\\iedcs-server";

        // Opening encfs encrypted volume given password and path
        EncFSVolume volume = null;
        String password = "iedcs";  //hardcoded volume password
        try {
            volume = new EncFSVolumeBuilder().withRootPath(encrypt_dir)
                    .withPassword(password).buildVolume();

        } catch (EncFSInvalidPasswordException e) {
            System.out.println("Invalid password!");
            System.exit(1);
        } catch (EncFSException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        EncFSFile root = volume.getRootDir();
        EncFSFile[] root_files = root.listFiles();
        for(EncFSFile f: root_files){
           // System.out.println(f.getPath());
            EncFSFile x[] = f.listFiles();
            for ( EncFSFile g : x){
              //  System.out.println(g.getPath());
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        EncFSFile enc_cert = volume.getFile("/certificados/EC de Autenticacao do Cartao de Cidadao 0008.pem");
        EncFSUtil.copyWholeStreamAndCloseInput(new EncFSFileInputStream(enc_cert),baos);
        cert_byte = baos.toByteArray();
        //FileInputStream is = new FileInputStream (new File("C:\\Users\\Andre\\Documents\\certificados\\EC de Autenticacao do Cartao de Cidadao 0008.pem"));
        ByteArrayInputStream is = new ByteArrayInputStream(cert_byte);
        X509Certificate cert_auth08 = (X509Certificate)certFactory.generateCertificate(is);
        //PublicKey key_auth08 = cert_auth08.getPublicKey();
        System.out.println(cert_auth08.getSubjectDN().getName());
        baos.flush();

        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        // cert_byte = cartao02.certif;
        EncFSFile enc_cert2 = volume.getFile("/certificados/Cartao de Cidadao 002.pem");
        EncFSUtil.copyWholeStreamAndCloseInput(new EncFSFileInputStream(enc_cert2),baos2);
        cert_byte = baos2.toByteArray();
       // certFactory = CertificateFactory.getInstance("X.509");
        //is = new FileInputStream (new File("C:\\Users\\Andre\\Documents\\certificados\\Cartao de Cidadao 002.pem"));
        is = new ByteArrayInputStream(cert_byte);
        X509Certificate cert_cartao02 = (X509Certificate)certFactory.generateCertificate(is);
        System.out.println(cert_cartao02.getSubjectDN().getName());
        baos.flush();

        ByteArrayOutputStream baos3 = new ByteArrayOutputStream();
        //cert_byte = cartao02.certif;
        EncFSFile enc_cert3 = volume.getFile("/certificados/ECRaizEstado_novo_assinado_GTE.pem");
        EncFSUtil.copyWholeStreamAndCloseInput(new EncFSFileInputStream(enc_cert3),baos3);
        cert_byte = baos3.toByteArray();
      //  certFactory = CertificateFactory.getInstance("X.509");
       // is = new FileInputStream (new File("C:\\Users\\Andre\\Documents\\certificados\\ECRaizEstado_novo_assinado_GTE.pem"));
        is = new ByteArrayInputStream(cert_byte);
        X509Certificate cert_estado = (X509Certificate)certFactory.generateCertificate(is);
        System.out.println(cert_estado.getSubjectDN().getName());
        baos.flush();

        ByteArrayOutputStream baos4 = new ByteArrayOutputStream();
        EncFSFile enc_cert4 = volume.getFile("/certificados/GTEGlobalRoot.pem");
        EncFSUtil.copyWholeStreamAndCloseInput(new EncFSFileInputStream(enc_cert4),baos4);
        cert_byte = baos4.toByteArray();
       // CertificateFactory fact = CertificateFactory.getInstance("X.509");
        //is = new FileInputStream (new File("C:\\Users\\Andre\\Documents\\certificados\\GTEGlobalRoot.pem"));
        is = new ByteArrayInputStream(cert_byte);
        X509Certificate cert_root = (X509Certificate) certFactory.generateCertificate(is);
        PublicKey key_root = cert_root.getPublicKey();
        System.out.println(cert_root.getSubjectDN().getName());
        baos.flush();


        String pkcs11Config = "name = CartaoCidadao"+System.getProperty("line.separator")+"library = c:\\windows\\system32\\pteidpkcs11.dll";
        byte[] pkcs11configBytes = pkcs11Config.getBytes();
        ByteArrayInputStream configStream = new ByteArrayInputStream(pkcs11configBytes);
        final Provider pkcs11Provider = new sun.security.pkcs11.SunPKCS11(configStream);
        System.out.println(pkcs11Provider.getInfo());
        Security.addProvider(pkcs11Provider);



        cert_citizencc.verify(cert_auth08.getPublicKey());
        cert_auth08.verify(cert_cartao02.getPublicKey());
        cert_cartao02.verify(cert_estado.getPublicKey());
        cert_estado.verify(cert_root.getPublicKey());
        cert_citizencc.checkValidity();
        cert_auth08.checkValidity();
        cert_cartao02.checkValidity();
        cert_estado.checkValidity();
        cert_root.checkValidity();

        System.out.println("certificates validated");


        return Response.status(200).entity("authenticated").build();
    }




}