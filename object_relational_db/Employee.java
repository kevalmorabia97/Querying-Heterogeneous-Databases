package object_relational_db;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Employee implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id int ecode;
	String ename;
	int sal;
	int yearBonus;
	int did;
	
	public Employee(int ecode, String ename, int sal, int yearBonus, int did) {
		this.ecode = ecode;
		this.ename = ename;
		this.sal = sal;
		this.yearBonus = yearBonus;
		this.did = did;
	}
}
