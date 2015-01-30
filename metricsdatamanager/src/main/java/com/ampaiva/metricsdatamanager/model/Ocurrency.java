package com.ampaiva.metricsdatamanager.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the ocurrencies database table.
 * 
 */
@Entity
@Table(name="ocurrencies")
@NamedQuery(name="Ocurrency.findAll", query="SELECT o FROM Ocurrency o")
public class Ocurrency implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE)
	@Column(unique=true, nullable=false)
	private int id;

	private int begincolumn;

	private int beginline;

	private int endcolumn;

	private int endline;

	private int type;

	//bi-directional many-to-one association to Resource
	@ManyToOne(cascade={CascadeType.ALL})
	@JoinColumn(name="resource", nullable=false)
	private Resource resourceBean;

	public Ocurrency() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getBegincolumn() {
		return this.begincolumn;
	}

	public void setBegincolumn(int begincolumn) {
		this.begincolumn = begincolumn;
	}

	public int getBeginline() {
		return this.beginline;
	}

	public void setBeginline(int beginline) {
		this.beginline = beginline;
	}

	public int getEndcolumn() {
		return this.endcolumn;
	}

	public void setEndcolumn(int endcolumn) {
		this.endcolumn = endcolumn;
	}

	public int getEndline() {
		return this.endline;
	}

	public void setEndline(int endline) {
		this.endline = endline;
	}

	public int getType() {
		return this.type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Resource getResourceBean() {
		return this.resourceBean;
	}

	public void setResourceBean(Resource resourceBean) {
		this.resourceBean = resourceBean;
	}

}