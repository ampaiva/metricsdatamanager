package com.ampaiva.metricsdatamanager.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * The persistent class for the sources database table.
 * 
 */
@Entity
@Table(name = "methods")
@NamedQuery(name = "Method.findAll", query = "SELECT m FROM Method m")
public class Method implements Serializable {
    private static final long serialVersionUID = 1L;

    //bi-directional many-to-one association to Repository
    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "repository", nullable = false)
    private Repository repositoryBean;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(unique = true, nullable = false)
    private int id;

    @Column(length = 255)
    private String name;

    @Lob
    private String source;

    @OneToMany(mappedBy = "methodBean", cascade = { CascadeType.ALL })
    private List<Call> calls;

    @OneToMany(mappedBy = "copy", cascade = { CascadeType.ALL })
    private List<Clone> copies;

    @OneToMany(mappedBy = "paste", cascade = { CascadeType.ALL })
    private List<Clone> pastes;

    public Method() {
    }

    public Method(String methodName, String methodSource) {
        this.name = methodName;
        this.source = methodSource;
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
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Repository getRepositoryBean() {
        return repositoryBean;
    }

    public void setRepositoryBean(Repository repositoryBean) {
        this.repositoryBean = repositoryBean;
    }

    public List<Call> getCalls() {
        return calls;
    }

    public void setCalls(List<Call> calls) {
        this.calls = calls;
    }

    @Override
    public String toString() {
        return "Method [name=" + name + "]";
    }
}
