package com.ampaiva.metricsdatamanager.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * The persistent class for the repositories database table.
 * 
 */
@Entity
@Table(name = "repositories")
@NamedQueries({ @NamedQuery(name = "Repository.findAll", query = "SELECT r FROM Repository r"),
        @NamedQuery(name = "Repository.findByLocation", query = "SELECT r FROM Repository r WHERE r.location=?1"),
        @NamedQuery(name = "Repository.findById", query = "SELECT r FROM Repository r WHERE r.id=?1") })
public class Repository implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(unique = true, nullable = false)
    private int id;

    @Column(length = 255, unique = true, nullable = false)
    private String location;

    //bi-directional many-to-one association to Method
    @OneToMany(mappedBy = "repositoryBean", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    private List<Method> methods;

    //bi-directional many-to-one association to Analyse
    @OneToMany(mappedBy = "repositoryBean", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    private List<Analyse> analysis;

    public Repository() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<Method> getMethods() {
        return this.methods;
    }

    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }

    public Method addMethod(Method method) {
        getMethods().add(method);
        method.setRepositoryBean(this);

        return method;
    }

    public Method removeMethod(Method method) {
        getMethods().remove(method);
        method.setRepositoryBean(null);

        return method;
    }

    public List<Analyse> getAnalysis() {
        return analysis;
    }

    public void setAnalysis(List<Analyse> analysis) {
        this.analysis = analysis;
    }

    public Analyse addAnalyse(Analyse analyse) {
        getAnalysis().add(analyse);
        analyse.setRepositoryBean(this);

        return analyse;
    }

    public Analyse removeAnalyse(Analyse analyse) {
        getAnalysis().remove(analyse);
        analyse.setRepositoryBean(null);

        return analyse;
    }

    @Override
    public String toString() {
        return "Repository [id=" + id + ", location=" + location + "]";
    }

}