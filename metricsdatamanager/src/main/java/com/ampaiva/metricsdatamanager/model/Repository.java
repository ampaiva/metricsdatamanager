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

import org.eclipse.persistence.annotations.CascadeOnDelete;

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

    //bi-directional many-to-one association to Analyse
    @OneToMany(mappedBy = "repositoryBean", orphanRemoval = true, cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    @CascadeOnDelete
    private List<Analyse> analysis;

    //bi-directional many-to-one association to Unit
    @OneToMany(mappedBy = "repositoryBean", orphanRemoval = true, cascade = {
            CascadeType.ALL }, fetch = FetchType.EAGER)
    @CascadeOnDelete
    private List<Unit> units;

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

    public List<Analyse> getAnalysis() {
        return this.analysis;
    }

    public void setAnalysis(List<Analyse> analysis) {
        this.analysis = analysis;
    }

    public Analyse addAnalysi(Analyse analysi) {
        getAnalysis().add(analysi);
        analysi.setRepositoryBean(this);

        return analysi;
    }

    public Analyse removeAnalysi(Analyse analysi) {
        getAnalysis().remove(analysi);
        analysi.setRepositoryBean(null);

        return analysi;
    }

    public List<Unit> getUnits() {
        return this.units;
    }

    public void setUnits(List<Unit> units) {
        this.units = units;
    }

    public Unit addUnit(Unit unit) {
        getUnits().add(unit);
        unit.setRepositoryBean(this);

        return unit;
    }

    public Unit removeUnit(Unit unit) {
        getUnits().remove(unit);
        unit.setRepositoryBean(null);

        return unit;
    }

    @Override
    public String toString() {
        return "Repository [id=" + id + ", location=" + location + "]";
    }

}