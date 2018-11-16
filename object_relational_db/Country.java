package object_relational_db;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Country implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Id String ccode;
	String cname;
	String capital;
	long population;
	String continent;
	
	public Country(String ccode, String cname, String capital, long population, String continent) {
		this.ccode = ccode;
		this.cname = cname;
		this.capital = capital;
		this.population = population;
		this.continent = continent;
	}
}
