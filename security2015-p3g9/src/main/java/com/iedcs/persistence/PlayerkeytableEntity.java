package com.iedcs.persistence;

import javax.persistence.*;

/**
 * Created by Andre on 15-11-2015.
 */
@Entity
@Table(name = "playerkeytable", schema = "", catalog = "userdb")
public class PlayerkeytableEntity {
    private String user;
    private String playerkey;
    private int id;

    @Basic
    @Column(name = "user")
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Basic
    @Column(name = "playerkey")
    public String getPlayerkey() {
        return playerkey;
    }

    public void setPlayerkey(String playerkey) {
        this.playerkey = playerkey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerkeytableEntity that = (PlayerkeytableEntity) o;

        if (user != null ? !user.equals(that.user) : that.user != null) return false;
        if (playerkey != null ? !playerkey.equals(that.playerkey) : that.playerkey != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = user != null ? user.hashCode() : 0;
        result = 31 * result + (playerkey != null ? playerkey.hashCode() : 0);
        return result;
    }

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
