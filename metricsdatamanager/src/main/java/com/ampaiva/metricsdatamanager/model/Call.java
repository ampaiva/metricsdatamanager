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

    @Column(nullable = false)
    private int beglin;

    @Column(nullable = false)
    private int begcol;

    @Column(nullable = false)
    private int endlin;

    @Column(nullable = false)
    private int endcol;

    //bi-directional many-to-one association to Method
    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "method", nullable = false)
    private Method methodBean;

    //bi-directional many-to-one association to Sequence
    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "sequence", nullable = false)
    private Sequence sequenceBean;

    @OneToMany(mappedBy = "begin", orphanRemoval = true, cascade = { CascadeType.ALL })
    @CascadeOnDelete
    private List<Clone> begins;

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

    public List<Clone> getBegins() {
        return this.begins;
    }

    public void setBegins(List<Clone> begins) {
        this.begins = begins;
    }

    public int getBeglin() {
        return beglin;
    }

    public void setBeglin(int beglin) {
        this.beglin = beglin;
    }

    public int getBegcol() {
        return begcol;
    }

    public void setBegcol(int begcol) {
        this.begcol = begcol;
    }

    public int getEndlin() {
        return endlin;
    }

    public void setEndlin(int endlin) {
        this.endlin = endlin;
    }

    public int getEndcol() {
        return endcol;
    }

    public void setEndcol(int endcol) {
        this.endcol = endcol;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + begcol;
        result = prime * result + beglin;
        result = prime * result + endcol;
        result = prime * result + endlin;
        result = prime * result + ((methodBean == null) ? 0 : methodBean.hashCode());
        result = prime * result + position;
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
        Call other = (Call) obj;
        if (begcol != other.begcol) {
            return false;
        }
        if (beglin != other.beglin) {
            return false;
        }
        if (endcol != other.endcol) {
            return false;
        }
        if (endlin != other.endlin) {
            return false;
        }
        if (methodBean == null) {
            if (other.methodBean != null) {
                return false;
            }
        } else if (!methodBean.equals(other.methodBean)) {
            return false;
        }
        if (position != other.position) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Call [id=" + id + ", position=" + position + ", beglin=" + beglin + ", begcol=" + begcol + ", endlin="
                + endlin + ", endcol=" + endcol + ", methodBean=" + methodBean + ", sequenceBean=" + sequenceBean + "]";
    }
}