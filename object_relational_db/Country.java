package object_relational_db;

import java.io.Serializable;

import javax.persistence.Entity;

@Entity
public class Country implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String cname;
	private String capital;
	private long population;
	private String continent;
	
	public Country(String cname, String capital, long population, String continent) {
		this.cname = cname;
		this.capital = capital;
		this.population = population;
		this.continent = continent;
	}

	public String getCname() {
		return cname;
	}

	public void setCname(String cname) {
		this.cname = cname;
	}

	public String getCapital() {
		return capital;
	}

	public void setCapital(String capital) {
		this.capital = capital;
	}

	public long getPopulation() {
		return population;
	}

	public void setPopulation(long population) {
		this.population = population;
	}

	public String getContinent() {
		return continent;
	}

	public void setContinent(String continent) {
		this.continent = continent;
	}
}
