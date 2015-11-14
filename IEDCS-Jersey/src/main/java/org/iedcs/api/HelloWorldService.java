package org.iedcs.api;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.iedcs.security.*;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Security;
import java.util.Base64;


@Path("/hello")
public class HelloWorldService {

    @GET
    @Path("/{param}")
    public Response getMessage(@PathParam("param") String message)throws Exception {

        String output = "Downloading: " + message;
        CipherEbook.cipher_file_1key("C:\\Users\\Andre\\Documents\\ebooks\\"+message+".epub", FileKeyFactory.getKey()); // cipher ebook with file key
        byte[] wrappedKey = KeyWrapper.wrapKey_3(DeviceKeyFactory.getDeviceKey(), UserKeyFactory.getKey(), PlayerKeyFactory.getKey(),FileKeyFactory.getKey()); // cipher file key with device,user and player key
        MakeHeader.make(new File("C:\\Users\\Andre\\Documents\\ebooks\\"+message+".epub"),wrappedKey,new File("C:\\Users\\Andre\\Documents\\ebooks\\"+message+"h.epub"));
        return Response.status(200).entity(output).build();
    }

    @POST
    @Path("/dec/")
    public Response sendCryptoinfo(byte[] message)throws Exception {



        SecretKey unwrappedKey = KeyWrapper.unwrapKey_3(DeviceKeyFactory.getDeviceKey(),UserKeyFactory.getKey(),PlayerKeyFactory.getKey(),message);

        //return Response.status(200).entity(output).build();

        String output = "testing..."+ new String(unwrappedKey.getEncoded());

        File fout = new File("C:\\Users\\Andre\\Documents\\ebooks\\file_key");
        FileOutputStream out = new FileOutputStream(fout);
        out.write(unwrappedKey.getEncoded());
        out.close();

        return Response.status(200).entity(output).build();
    }

    @GET
    @Path("/receive")
    public Response receiveCryptoinfo()throws Exception{
        byte[] buffer = new  byte[32];
        File fout = new File("C:\\Users\\Andre\\Documents\\ebooks\\file_key");
        FileInputStream fin = new FileInputStream(fout);
        int read;
        fin.read(buffer);

        return Response.status(200).entity(buffer).build();
    }


}