package com.ampaiva.metricsdatamanager.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.CascadeOnDelete;

/**
 * The persistent class for the units database table.
 * 
 */
@Entity
@Table(name = "units", indexes = {
        @Index(name = "units_repository_name_idx", columnList = "repository,name", unique = true) })
@NamedQueries({ @NamedQuery(name = "Unit.findAll", query = "SELECT u FROM Unit u"),
        @NamedQuery(name = "Unit.findById", query = "SELECT u FROM Unit u WHERE u.id=?1") })
public class Unit implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(unique = true, nullable = false)
    private int id;

    @Column(nullable = false, length = 255)
    private String name;

    @Lob
    @Column(nullable = false)
    private String source;

    @OneToMany(mappedBy = "unitBean", orphanRemoval = true, cascade = { CascadeType.ALL })
    @CascadeOnDelete
    private List<Method> methods;

    //bi-directional many-to-one association to Repository
    @ManyToOne
    @JoinColumn(name = "repository", nullable = false)
    private Repository repositoryBean;

    public Unit() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<Method> getMethods() {
        return this.methods;
    }

    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }

    public Repository getRepositoryBean() {
        return this.repositoryBean;
    }

    public void setRepositoryBean(Repository repositoryBean) {
        this.repositoryBean = repositoryBean;
    }

    @Override
    public String toString() {
        return "Unit [id=" + id + ", name=" + name + ", repositoryBean=" + repositoryBean + "]";
    }

}