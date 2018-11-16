package object_relational_db;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Department implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id private int dcode;
	private String dname;
	private String dloc;
	private long phone_no;
	
	public Department(int dcode, String dname, String dloc, long phone_no) {
		this.dcode = dcode;
		this.dname = dname;
		this.dloc = dloc;
		this.phone_no = phone_no;
	}

	public int getDcode() {
		return dcode;
	}

	public void setDcode(int dcode) {
		this.dcode = dcode;
	}

	public String getDname() {
		return dname;
	}

	public void setDname(String dname) {
		this.dname = dname;
	}

	public String getDloc() {
		return dloc;
	}

	public void setDloc(String dloc) {
		this.dloc = dloc;
	}

	public long getPhone_no() {
		return phone_no;
	}

	public void setPhone_no(long phone_no) {
		this.phone_no = phone_no;
	}
}
