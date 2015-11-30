package com.ampaiva.metricsdatamanager.model;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the units database table.
 * 
 */
@Entity
@Table(name="units")
@NamedQuery(name="Unit.findAll", query="SELECT u FROM Unit u")
public class Unit implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE)
	@Column(unique=true, nullable=false)
	private int id;

	@Column(nullable=false, length=255)
	private String name;

	@Lob
	@Column(nullable=false)
	private String source;

	//bi-directional many-to-one association to Method
	@OneToMany(mappedBy="unitBean")
	private List<Method> methods;

	//bi-directional many-to-one association to Repository
	@ManyToOne
	@JoinColumn(name="repository", nullable=false)
	private Repository repositoryBean;

	public Unit() {
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

	public String getSource() {
		return this.source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public List<Method> getMethods() {
		return this.methods;
	}

	public void setMethods(List<Method> methods) {
		this.methods = methods;
	}

	public Method addMethod(Method method) {
		getMethods().add(method);
		method.setUnitBean(this);

		return method;
	}

	public Method removeMethod(Method method) {
		getMethods().remove(method);
		method.setUnitBean(null);

		return method;
	}

	public Repository getRepositoryBean() {
		return this.repositoryBean;
	}

	public void setRepositoryBean(Repository repositoryBean) {
		this.repositoryBean = repositoryBean;
	}

}