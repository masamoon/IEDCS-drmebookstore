package com.iedcs.persistence;

import javax.persistence.*;

/**
 * Created by Andre on 07-11-2015.
 */
@Entity
@Table(name = "usercred", schema = "", catalog = "userdb")
public class UsercredEntity {
    private String username;
    private String passhash;
    private int id;
    private String userKey;

    @Basic
    @Column(name = "username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Basic
    @Column(name = "passhash")
    public String getPasshash() {
        return passhash;
    }

    public void setPasshash(String passhash) {
        this.passhash = passhash;
    }

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "user_key")
    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UsercredEntity that = (UsercredEntity) o;

        if (id != that.id) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;
        if (passhash != null ? !passhash.equals(that.passhash) : that.passhash != null) return false;
        if (userKey != null ? !userKey.equals(that.userKey) : that.userKey != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (passhash != null ? passhash.hashCode() : 0);
        result = 31 * result + id;
        result = 31 * result + (userKey != null ? userKey.hashCode() : 0);
        return result;
    }
}
