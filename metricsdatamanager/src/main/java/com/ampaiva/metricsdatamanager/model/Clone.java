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
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.CascadeOnDelete;

/**
 * The persistent class for the clones database table.
 * 
 */
@Entity
@Table(name = "clones", indexes = {
        @Index(name = "analyse_copy_paste_idx", columnList = "analyse,copy,paste", unique = true) })
@NamedQuery(name = "Clone.findAll", query = "SELECT r FROM Clone r")
public class Clone implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(unique = true, nullable = false)
    private int id;

    //bi-directional many-to-one association to Repository
    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "analyse", nullable = false)
    private Analyse analyseBean;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "copy", nullable = false)
    private Method copy;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "paste", nullable = false)
    private Method paste;

    @OneToMany(mappedBy = "cloneBean", orphanRemoval = true, cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    @CascadeOnDelete
    private List<Clonecall> clonecalls;

    public Clone() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Clonecall> getClonecalls() {
        return this.clonecalls;
    }

    public void setClonecalls(List<Clonecall> clonecalls) {
        this.clonecalls = clonecalls;
    }

    public Clonecall addClonecall(Clonecall clonecall) {
        getClonecalls().add(clonecall);
        clonecall.setCloneBean(this);

        return clonecall;
    }

    public Clonecall removeClonecall(Clonecall clonecall) {
        getClonecalls().remove(clonecall);
        clonecall.setCloneBean(null);

        return clonecall;
    }

    public Analyse getAnalyseBean() {
        return this.analyseBean;
    }

    public void setAnalyseBean(Analyse analyse) {
        this.analyseBean = analyse;
    }

    public Method getCopy() {
        return this.copy;
    }

    public void setCopy(Method copy) {
        this.copy = copy;
    }

    public Method getPaste() {
        return this.paste;
    }

    public void setPaste(Method paste) {
        this.paste = paste;
    }

}