package com.ampaiva.metricsdatamanager.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

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
    private Call copy;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "paste", nullable = false)
    private Call paste;

    public Clone() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Analyse getAnalyseBean() {
        return this.analyseBean;
    }

    public void setAnalyseBean(Analyse analyse) {
        this.analyseBean = analyse;
    }

    public Call getCopy() {
        return this.copy;
    }

    public void setCopy(Call copy) {
        this.copy = copy;
    }

    public Call getPaste() {
        return this.paste;
    }

    public void setPaste(Call paste) {
        this.paste = paste;
    }

    @Override
    public String toString() {
        return "Clone [id=" + id + ", analyseBean=" + analyseBean + ", copy=" + copy + ", paste=" + paste + "]";
    }

}