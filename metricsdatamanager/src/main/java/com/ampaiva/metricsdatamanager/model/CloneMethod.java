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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * The persistent class for the sources database table.
 * 
 */
@Entity
@Table(name = "clonemethods")
@NamedQuery(name = "CloneMethod.findAll", query = "SELECT m FROM CloneMethod m")
public class CloneMethod implements Serializable {
    private static final long serialVersionUID = 1L;

    //bi-directional many-to-one association to Repository
    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "clone", nullable = false)
    private Clone cloneBean;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(unique = true, nullable = false)
    private int id;

    @Column(length = 255)
    private String name;

    //bi-directional many-to-one association to Sequence
    @OneToMany(mappedBy = "methodBean", cascade = { CascadeType.ALL })
    private List<CloneCall> sequences;

    public CloneMethod() {
    }

    public CloneMethod(String methodName) {
        this.name = methodName;
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

    public Clone getRepositoryBean() {
        return cloneBean;
    }

    public void setCloneBean(Clone cloneBean) {
        this.cloneBean = cloneBean;
    }

    public List<CloneCall> getCalls() {
        return sequences;
    }

    public void setCalls(List<CloneCall> calls) {
        this.sequences = calls;
    }

    @Override
    public String toString() {
        return "CloneMethod [name=" + name + "]";
    }
}
