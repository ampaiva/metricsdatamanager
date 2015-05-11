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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.CascadeOnDelete;

/**
 * The persistent class for the repositories database table.
 * 
 */
@Entity
@Table(name = "clones")
@NamedQuery(name = "Clone.findAll", query = "SELECT r FROM Clone r")
public class Clone implements Serializable {
    private static final long serialVersionUID = 1L;

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

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(unique = true, nullable = false)
    private int id;

    @OneToMany(mappedBy = "clone", orphanRemoval = true, cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    @CascadeOnDelete
    private List<CloneCall> calls;

    public Clone() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Analyse getAnalyseBean() {
        return analyseBean;
    }

    public void setAnalyseBean(Analyse analyseBean) {
        this.analyseBean = analyseBean;
    }

    public Method getCopy() {
        return copy;
    }

    public void setCopy(Method copy) {
        this.copy = copy;
    }

    public Method getPaste() {
        return paste;
    }

    public void setPaste(Method paste) {
        this.paste = paste;
    }

    public List<CloneCall> getCalls() {
        return calls;
    }

    public void setCalls(List<CloneCall> calls) {
        this.calls = calls;
    }
}