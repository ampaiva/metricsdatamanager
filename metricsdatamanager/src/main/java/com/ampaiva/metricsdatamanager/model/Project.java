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
@Table(name = "projects")
@NamedQuery(name = "Project.findAll", query = "SELECT p FROM Project p")
public class Project implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(unique = true, nullable = false)
    private int id;

    @Column(length = 255)
    private String location;

    @Column(nullable = false, length = 255, unique = true)
    private String name;

    //bi-directional many-to-one association to Resource
    @OneToMany(mappedBy = "projectBean", cascade = { CascadeType.ALL })
    private List<Resource> resources;

    public Project() {
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

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Resource> getResources() {
        return this.resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public Resource addResource(Resource resource) {
        getResources().add(resource);
        resource.setProjectBean(this);

        return resource;
    }

    public Resource removeResource(Resource resource) {
        getResources().remove(resource);
        resource.setProjectBean(null);

        return resource;
    }

}