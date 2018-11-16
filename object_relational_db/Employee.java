package object_relational_db;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Employee implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id private int ecode;
	private String ename;
	private int sal;
	private int year_bonus;
	private int did;
	
	public Employee(int ecode, String ename, int sal, int year_bonus, int did) {
		this.ecode = ecode;
		this.ename = ename;
		this.sal = sal;
		this.year_bonus = year_bonus;
		this.did = did;
	}

	public int getEcode() {
		return ecode;
	}

	public void setEcode(int ecode) {
		this.ecode = ecode;
	}

	public String getEname() {
		return ename;
	}

	public void setEname(String ename) {
		this.ename = ename;
	}

	public int getSal() {
		return sal;
	}

	public void setSal(int sal) {
		this.sal = sal;
	}

	public int getYear_bonus() {
		return year_bonus;
	}

	public void setYear_bonus(int year_bonus) {
		this.year_bonus = year_bonus;
	}

	public int getDid() {
		return did;
	}

	public void setDid(int did) {
		this.did = did;
	}
}
