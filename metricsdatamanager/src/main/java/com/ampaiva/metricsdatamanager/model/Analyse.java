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
@Table(name = "analisys")
@NamedQuery(name = "Analyse.findAll", query = "SELECT a FROM Analyse a")
public class Analyse implements Serializable {
    private static final long serialVersionUID = 1L;

    //bi-directional many-to-one association to Repository
    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "repository", nullable = false)
    private Repository repositoryBean;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(unique = true, nullable = false)
    private int id;

    @Column
    private Integer minSeq;

    @Column
    private Integer maxDist;

    @OneToMany(mappedBy = "analyseBean", cascade = { CascadeType.ALL })
    private List<Clone> clones;

    public List<Clone> getClones() {
        return clones;
    }

    public void setClones(List<Clone> clones) {
        this.clones = clones;
    }

    public Analyse() {
    }

    public Analyse(int minSeq, int maxDist) {
        this.minSeq = minSeq;
        this.maxDist = maxDist;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getMinSeq() {
        return minSeq;
    }

    public void setMinSeq(Integer minSeq) {
        this.minSeq = minSeq;
    }

    public Integer getMaxDist() {
        return maxDist;
    }

    public void setMaxDist(Integer maxDist) {
        this.maxDist = maxDist;
    }

    public Repository getRepositoryBean() {
        return repositoryBean;
    }

    public void setRepositoryBean(Repository repositoryBean) {
        this.repositoryBean = repositoryBean;
    }

    @Override
    public String toString() {
        return "Analyse [id=" + id + ", minSeq=" + minSeq + ", maxDist=" + maxDist + ", repositoryBean="
                + repositoryBean + "]";
    }

}
