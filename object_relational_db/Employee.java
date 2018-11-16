package object_relational_db;

public class Employee {
	int ecode;
	String ename;
	int sal;
	int year_bonus;
	int did;
	
	public Employee(int ecode, String ename, int sal, int year_bonus, int did) {
		this.ecode = ecode;
		this.ename = ename;
		this.sal = sal;
		this.year_bonus = year_bonus;
		this.did = did;
	}
}
