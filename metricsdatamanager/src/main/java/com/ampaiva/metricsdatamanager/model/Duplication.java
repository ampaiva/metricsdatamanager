package com.ampaiva.metricsdatamanager.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the duplications database table.
 * 
 */
@Entity
@Table(name="duplications")
@NamedQuery(name="Duplication.findAll", query="SELECT d FROM Duplication d")
public class Duplication implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE)
	@Column(unique=true, nullable=false)
	private int id;

	@Column(nullable=false)
	private int copy;

	@Column(nullable=false)
	private int paste;

	public Duplication() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCopy() {
		return this.copy;
	}

	public void setCopy(int copy) {
		this.copy = copy;
	}

	public int getPaste() {
		return this.paste;
	}

	public void setPaste(int paste) {
		this.paste = paste;
	}

}