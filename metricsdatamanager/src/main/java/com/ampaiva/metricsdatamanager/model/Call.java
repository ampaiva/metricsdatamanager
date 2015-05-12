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
 * The persistent class for the sources database table.
 * 
 */
@Entity
@Table(name = "calls")
@NamedQuery(name = "Call.findAll", query = "SELECT m FROM Call m")
public class Call implements Serializable {
    private static final long serialVersionUID = 1L;

    //bi-directional many-to-one association to Method
    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "method", nullable = false)
    private Method methodBean;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(unique = true, nullable = false)
    private int id;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "sequence", nullable = false)
    private Sequence sequence;

    @Column(name = "position", nullable = false)
    private int position;

    @OneToMany(mappedBy = "copy", orphanRemoval = true, cascade = { CascadeType.ALL })
    @CascadeOnDelete
    private List<CloneCall> copies;

    @OneToMany(mappedBy = "paste", orphanRemoval = true, cascade = { CascadeType.ALL })
    @CascadeOnDelete
    private List<CloneCall> pastes;

    public Call() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Method getMethodBean() {
        return methodBean;
    }

    public void setMethodBean(Method methodBean) {
        this.methodBean = methodBean;
    }

    public Sequence getSequence() {
        return sequence;
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public List<CloneCall> getCopies() {
        return copies;
    }

    public void setCopies(List<CloneCall> copies) {
        this.copies = copies;
    }

    public List<CloneCall> getPastes() {
        return pastes;
    }

    public void setPastes(List<CloneCall> pastes) {
        this.pastes = pastes;
    }

    @Override
    public String toString() {
        return "Call [id=" + id + ", position=" + position + ", sequence=" + sequence + ", methodBean=" + methodBean
                + "]";
    }
}
