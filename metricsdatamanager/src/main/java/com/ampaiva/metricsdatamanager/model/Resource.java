package com.ampaiva.metricsdatamanager.model;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the resources database table.
 * 
 */
@Entity
@Table(name="resources")
@NamedQuery(name="Resource.findAll", query="SELECT r FROM Resource r")
public class Resource implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE)
	@Column(unique=true, nullable=false)
	private int id;

	@Column(length=255)
	private String name;

	//bi-directional many-to-one association to Ocurrency
	@OneToMany(mappedBy="resourceBean", cascade={CascadeType.ALL})
	private List<Ocurrency> ocurrencies;

	//bi-directional many-to-one association to Project
	@ManyToOne(cascade={CascadeType.ALL})
	@JoinColumn(name="project", nullable=false)
	private Project projectBean;

	public Resource() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Ocurrency> getOcurrencies() {
		return this.ocurrencies;
	}

	public void setOcurrencies(List<Ocurrency> ocurrencies) {
		this.ocurrencies = ocurrencies;
	}

	public Ocurrency addOcurrency(Ocurrency ocurrency) {
		getOcurrencies().add(ocurrency);
		ocurrency.setResourceBean(this);

		return ocurrency;
	}

	public Ocurrency removeOcurrency(Ocurrency ocurrency) {
		getOcurrencies().remove(ocurrency);
		ocurrency.setResourceBean(null);

		return ocurrency;
	}

	public Project getProjectBean() {
		return this.projectBean;
	}

	public void setProjectBean(Project projectBean) {
		this.projectBean = projectBean;
	}

}