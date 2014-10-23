package com.ampaiva.metricsdatamanager.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the metrics database table.
 * 
 */
@Entity
@Table(name="metrics")
@NamedQuery(name="Metric.findAll", query="SELECT m FROM Metric m")
public class Metric implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE)
	@Column(unique=true, nullable=false)
	private int id;

	@Column(length=255)
	private String location;

	@Column(nullable=false, length=255)
	private String name;

	public Metric() {
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

}