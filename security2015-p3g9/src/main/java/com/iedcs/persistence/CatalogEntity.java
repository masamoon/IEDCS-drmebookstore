package com.iedcs.persistence;

import javax.persistence.*;

/**
 * Created by Andre on 07-11-2015.
 */
@Entity
@Table(name = "catalog", schema = "", catalog = "userdb")
public class CatalogEntity {
    private int id;
    private String title;
    private String directory;

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Basic
    @Column(name = "directory")
    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CatalogEntity that = (CatalogEntity) o;

        if (id != that.id) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (directory != null ? !directory.equals(that.directory) : that.directory != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (directory != null ? directory.hashCode() : 0);
        return result;
    }
}
