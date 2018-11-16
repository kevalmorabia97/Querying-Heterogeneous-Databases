package object_relational_db;

public class Department {
	int dcode;
	String dname;
	String dloc;
	int phone_no;
	
	public Department(int dcode, String dname, String dloc, int phone_no) {
		this.dcode = dcode;
		this.dname = dname;
		this.dloc = dloc;
		this.phone_no = phone_no;
	}
}
