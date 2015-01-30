package com.ampaiva.metricsdatamanager.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * The persistent class for the projects database table.
 * 
 */
@Entity
@Table(name = "duplicationblocks")
@NamedQuery(name = "DuplicationBlock.findAll", query = "SELECT p FROM DuplicationBlock p")
public class DuplicationBlock implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(unique = true, nullable = false)
    private int id;

    //bi-directional many-to-one association to DuplicationSection
    @OneToMany(mappedBy = "duplicationBlockBean", cascade = { CascadeType.ALL })
    private List<DuplicationSection> duplicationSections;

    public DuplicationBlock() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<DuplicationSection> getDuplicationSection() {
        return this.duplicationSections;
    }

    public void setDuplicationSection(List<DuplicationSection> duplicationSection) {
        this.duplicationSections = duplicationSection;
    }

    public DuplicationSection addDuplicationSection(DuplicationSection duplicationBlockBean) {
        getDuplicationSection().add(duplicationBlockBean);
        duplicationBlockBean.setDuplicationBlockBean(this);

        return duplicationBlockBean;
    }

    public DuplicationSection removeResource(DuplicationSection duplicationSection) {
        getDuplicationSection().remove(duplicationSection);
        duplicationSection.setDuplicationBlockBean(null);

        return duplicationSection;
    }

}