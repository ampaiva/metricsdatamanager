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
 * The persistent class for the DuplicationSection database table.
 * 
 */
@Entity
@Table(name = "duplicationsection")
@NamedQuery(name = "DuplicationSection.findAll", query = "SELECT o FROM DuplicationSection o")
public class DuplicationSection implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(unique = true, nullable = false)
    private int id;

    private int beginline;

    private int endline;

    //bi-directional many-to-one association to Resource
    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "resource", nullable = false)
    private Resource resourceBean;

    //bi-directional many-to-one association to DuplicationBlock
    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "duplicationblock", nullable = false)
    private DuplicationBlock duplicationBlockBean;

    public DuplicationSection() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBeginline() {
        return this.beginline;
    }

    public void setBeginline(int beginline) {
        this.beginline = beginline;
    }

    public int getEndline() {
        return this.endline;
    }

    public void setEndline(int endline) {
        this.endline = endline;
    }

    public Resource getResourceBean() {
        return this.resourceBean;
    }

    public void setResourceBean(Resource resourceBean) {
        this.resourceBean = resourceBean;
    }

    public DuplicationBlock getDuplicationBlock() {
        return this.duplicationBlockBean;
    }

    public void setDuplicationBlockBean(DuplicationBlock duplicationBlockBean) {
        this.duplicationBlockBean = duplicationBlockBean;
    }

}