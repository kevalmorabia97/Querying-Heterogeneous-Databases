package object_relational_db;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Department implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id int dcode;
	String dname;
	String dloc;
	long phoneNo;
	
	public Department(int dcode, String dname, String dloc, long phoneNo) {
		this.dcode = dcode;
		this.dname = dname;
		this.dloc = dloc;
		this.phoneNo = phoneNo;
	}
}
