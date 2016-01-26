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
    @JoinColumn(name = "begin", nullable = false)
    private Call begin;

    @Column(nullable = false)
    private Integer size;

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

    public Call getBegin() {
        return begin;
    }

    public void setBegin(Call begin) {
        this.begin = begin;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((begin == null) ? 0 : begin.hashCode());
        result = prime * result + ((size == null) ? 0 : size.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Clone other = (Clone) obj;
        if (begin == null) {
            if (other.begin != null) {
                return false;
            }
        } else if (!begin.equals(other.begin)) {
            return false;
        }
        if (size == null) {
            if (other.size != null) {
                return false;
            }
        } else if (!size.equals(other.size)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Clone [id=" + id + ", analyseBean=" + analyseBean + ", begin=" + begin + ", size=" + size + "]";
    }

}