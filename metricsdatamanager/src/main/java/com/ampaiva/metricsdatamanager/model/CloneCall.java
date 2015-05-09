package com.ampaiva.metricsdatamanager.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * The persistent class for the sources database table.
 * 
 */
@Entity
@Table(name = "clonecalls")
@NamedQuery(name = "CloneCall.findAll", query = "SELECT m FROM CloneCall m")
public class CloneCall implements Serializable {
    private static final long serialVersionUID = 1L;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "clone", nullable = false)
    private Clone clone;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "copy", nullable = false)
    private Call copy;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "paste", nullable = false)
    private Call paste;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(unique = true, nullable = false)
    private int id;

    public CloneCall() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Clone getClone() {
        return clone;
    }

    public void setClone(Clone clone) {
        this.clone = clone;
    }

    public Call getCopy() {
        return copy;
    }

    public void setCopy(Call copy) {
        this.copy = copy;
    }

    public Call getPaste() {
        return paste;
    }

    public void setPaste(Call paste) {
        this.paste = paste;
    }
}
