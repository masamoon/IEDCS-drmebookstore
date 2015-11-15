package org.iedcs.api;

import javax.crypto.SecretKey;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.iedcs.persistence.PlayerkeytableEntity;
import com.iedcs.security.*;
import org.hibernate.jpa.HibernatePersistenceProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;


@Path("/hello")
public class HelloWorldService {
    //String local_directory = "C:\\Users\\Andre\\Documents\\ebooks\\"; //static directory
    //String local_directory = WorkingDirectories.getLocalDirectory(); //
    String local_directory = WorkingDirectories.getWorking_directory();
    //String local_directory = "ebooks/";


    @GET
    @Path("/{param}")
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




}