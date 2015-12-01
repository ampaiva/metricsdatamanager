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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.CascadeOnDelete;

/**
 * The persistent class for the methods database table.
 * 
 */
@Entity
@Table(name = "methods")
@NamedQueries({ @NamedQuery(name = "Method.findAll", query = "SELECT m FROM Method m"),
        @NamedQuery(name = "Method.findById", query = "SELECT r FROM Method r WHERE r.id=?1") })
public class Method implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(unique = true, nullable = false)
    private int id;

    @Column(length = 255)
    private String name;

    @Lob
    private String source;

    @OneToMany(mappedBy = "methodBean", orphanRemoval = true, cascade = { CascadeType.ALL })
    @CascadeOnDelete
    private List<Call> calls;

    @OneToMany(mappedBy = "copy", orphanRemoval = true, cascade = { CascadeType.ALL })
    @CascadeOnDelete
    private List<Clone> copies;

    @OneToMany(mappedBy = "paste", orphanRemoval = true, cascade = { CascadeType.ALL })
    @CascadeOnDelete
    private List<Clone> pastes;

    //bi-directional many-to-one association to Unit
    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "unit", nullable = false)
    private Unit unitBean;

    public Method() {
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

    public List<Call> getCalls() {
        return this.calls;
    }

    public void setCalls(List<Call> calls) {
        this.calls = calls;
    }

    public List<Clone> getCopies() {
        return this.copies;
    }

    public void setCopies(List<Clone> copies) {
        this.copies = copies;
    }

    public List<Clone> getClones2() {
        return this.pastes;
    }

    public void setPastes(List<Clone> pastes) {
        this.pastes = pastes;
    }

    public Unit getUnitBean() {
        return this.unitBean;
    }

    public void setUnitBean(Unit unitBean) {
        this.unitBean = unitBean;
    }

    @Override
    public String toString() {
        return "Method [id=" + id + ", name=" + name + ", unitBean=" + unitBean + "]";
    }

}