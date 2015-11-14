package com.iedcs.security;

/**
 * Created by Andre on 07-11-2015.
 */
public class SQLqueries {

    public static String select(String from, String param, String value){
        return "from "+from+" CatalogEntity where "+param+" = :"+value;
    }


}
