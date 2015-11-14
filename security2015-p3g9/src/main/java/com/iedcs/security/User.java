
package com.iedcs.security;

/**
 *
 * @author Andre
 */
public class User {
    private String username; 
    private String password; 
    private String realname; 

    public User(String username, String password, String realname) {
        this.username = username;
        this.password = password;
        this.realname = realname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }
    
    
    
    
    
}
