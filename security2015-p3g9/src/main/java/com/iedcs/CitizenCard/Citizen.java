package com.iedcs.CitizenCard;

/**
 * Created by Andre on 29-11-2015.
 */
public class Citizen {

    private String id;
    private String name;
    private String pass;
    private String user_key;
    private String public_key;

    public String getId() {
        return id;
    }

    public String getPublic_key(){ return public_key; }

    public void setPublic_key(String public_key){ this.public_key = public_key; }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getUser_key() {
        return user_key;
    }

    public void setUser_key(String user_key) {
        this.user_key = user_key;
    }
}
