package com.iedcs.security;

import javax.crypto.SecretKey;
import java.io.*;

/**
 * Created by Andre on 13-11-2015.
 */
public class MakeHeader {

    public static void main(String[] args)throws Exception{
        /*File f = new File("C:\\Users\\Andre\\Documents\\ebooks\\meinkampf.epub");
        File output = new File("C:\\Users\\Andre\\Documents\\ebooks\\meinkampfhead.epub");
        byte[] file_key = FileKeyFactory.wrappedKey();
        make(f,file_key ,output);
        String header = readHeader(output);

        System.out.println("KEY");
        System.out.println(new String(file_key));
        System.out.println("HEADER");
        System.out.println(header);*/

        File f = new File("C:\\Users\\Andre\\Documents\\ebooks\\testheader.txt");
        File output = new File("C:\\Users\\Andre\\Documents\\ebooks\\testheaderout.txt");
        File output_noheader = new File("C:\\Users\\Andre\\Documents\\ebooks\\testnoheader.txt");
        byte[] file_key = FileKeyFactory.wrappedKey();
        make(f,file_key,output);
        System.out.println("header: "+readHeader(output));
        byte[] header = removeHeader(output, output_noheader);

    }

    public static String readHeader(File file){

       FileInputStream fin = null;
        String s = "";
        try {
            // create FileInputStream object
            fin = new FileInputStream(file);

            byte fileContent[] = new byte[32];

            // Reads up to certain bytes of data from this input stream into an array of bytes.
            fin.read(fileContent);
            //create string from byte array
             s = new String(fileContent);
            //System.out.println("File content: " + s);
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        }
        catch (IOException ioe) {
            System.out.println("Exception while reading file " + ioe);
        }
        finally {
            // close the streams using close method
            try {
                if (fin != null) {
                    fin.close();
                }
            }
            catch (IOException ioe) {
                System.out.println("Error while closing stream: " + ioe);
            }
        }

        return s;
    }

    public static void make(File file, byte[] header, File output_file ) throws Exception{

            FileInputStream fin = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int read;
            FileOutputStream fo = new FileOutputStream(output_file);
            fo.write(header);
            while((read = fin.read(buffer))!=-1){
                fo.write(buffer,0,read);

            }


    }

    public static byte[] removeHeader(File file, File output_file)throws Exception{


        FileInputStream fin = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        byte[] header = new byte[32];
        int read;
        FileOutputStream fo = new FileOutputStream(output_file);
        //fo.write(header);
        fin.read(header,0,32);
        System.out.println("HEADER");
        System.out.println(new String(header));
       // fin.skip(32);
        while((read = fin.read(buffer))!=-1){
            fo.write(buffer,0,read);

        }
        return header;
    }
}
