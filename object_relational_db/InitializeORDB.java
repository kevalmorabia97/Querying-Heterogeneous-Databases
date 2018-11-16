package object_relational_db;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class InitializeORDB {
	
	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("ordbms.odb");
		EntityManager em = emf.createEntityManager();

		em.getTransaction().begin();

		for (int i = 1; i <= 5; i++) {
			Employee e = new Employee(i,"Employee "+i, i*12345, i*1000, i%3);
			em.persist(e);
		}
		
		for (int i = 1; i <= 3; i++) {
			Department d = new Department(i, "Department "+i, "DLoc "+i, i*3425351808l);
			em.persist(d);
		}
		
		for(int i = 1; i <= 5; i++) {
			Country c = new Country("Country "+i, "Capital "+i, i*8452487000l, "Continent "+i);
			em.persist(c);
		}
		
		em.getTransaction().commit();

		em.close();
		emf.close();
	}
}
