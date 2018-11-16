package object_relational_db;

public class Country {
	String cname;
	String capital;
	long population;
	String continent;
	
	public Country(String cname, String capital, long population, String continent) {
		this.cname = cname;
		this.capital = capital;
		this.population = population;
		this.continent = continent;
	}
}
