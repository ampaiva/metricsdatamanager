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

import org.eclipse.persistence.annotations.CascadeOnDelete;

/**
 * The persistent class for the calls database table.
 * 
 */
@Entity
@Table(name = "calls")
@NamedQuery(name = "Call.findAll", query = "SELECT c FROM Call c")
public class Call implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(unique = true, nullable = false)
    private int id;

    @Column(nullable = false)
    private int position;

    //bi-directional many-to-one association to Method
    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "method", nullable = false)
    private Method methodBean;

    //bi-directional many-to-one association to Sequence
    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "sequence", nullable = false)
    private Sequence sequenceBean;

    @OneToMany(mappedBy = "copy", orphanRemoval = true, cascade = { CascadeType.ALL })
    @CascadeOnDelete
    private List<Clonecall> copies;

    @OneToMany(mappedBy = "paste", orphanRemoval = true, cascade = { CascadeType.ALL })
    @CascadeOnDelete
    private List<Clonecall> pastes;

    public Call() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPosition() {
        return this.position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Method getMethodBean() {
        return this.methodBean;
    }

    public void setMethodBean(Method methodBean) {
        this.methodBean = methodBean;
    }

    public Sequence getSequenceBean() {
        return this.sequenceBean;
    }

    public void setSequenceBean(Sequence sequenceBean) {
        this.sequenceBean = sequenceBean;
    }

    public List<Clonecall> getCopies() {
        return this.copies;
    }

    public void setCopies(List<Clonecall> copies) {
        this.copies = copies;
    }

    public List<Clonecall> getPastes() {
        return this.pastes;
    }

    public void setPastes(List<Clonecall> pastes) {
        this.pastes = pastes;
    }

    @Override
    public String toString() {
        return "Call [id=" + id + ", position=" + position + ", methodBean=" + methodBean + ", sequenceBean="
                + sequenceBean + "]";
    }
}