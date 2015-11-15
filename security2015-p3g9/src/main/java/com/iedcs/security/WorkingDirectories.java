package com.iedcs.security;

/**
 * Created by Andre on 15-11-2015.
 */
public class WorkingDirectories {
    private static String working_directory;

    public static String getLocalDirectory(){
        return "C:\\Users\\Andre\\Documents\\ebooks\\";
    }

    public static String getRemoteDirectory(){
        return "C:\\Users\\Andre\\Documents\\ebooks\\server\\";
    }

    public static String getRelativeDirectory(){
        return "";
    }

    public static void setWorkDirectory(String dir){
        working_directory = dir;
    }

    public static String getWorking_directory(){
        return working_directory;
    }
}
