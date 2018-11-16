import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

public class Main {

	static String mysqlDBUserName = "root";
	static String mysqlDBPass = "root";

	static String ordbmsFile = "ordbms.odb";

	static String[] unifiedTables = new String[] {"UEmployee", "UDepartment", "UCountry"};

	// map from url of rel database to Database
	static HashMap<String, Database> unifiedRDBMS = new HashMap<>();
	static Database ORDBMS = new Database();
	
	static {
		// RelationalDATABASE 1	(TableColMapping and U2AMapping only for different names)	
		Database mySQLDB1 = new Database();
		mySQLDB1.addU2AMapping("UEmployee", "employee");
		mySQLDB1.addTableColMapping("UEmployee", "ecode", "eid"); // ecode in UEmployee = eid in employee of mysql1
		mySQLDB1.addTableColMapping("UEmployee", "sal", "wage");
		mySQLDB1.addU2AMapping("UDepartment", "department");
		mySQLDB1.addU2AMapping("UCountry", "country");
		unifiedRDBMS.put("jdbc:mysql://localhost:3306/mysql1", mySQLDB1);

		// RelationalDATABASE 2		
		Database mySQLDB2 = new Database();
		mySQLDB2.addU2AMapping("UEmployee", "empl");
		mySQLDB2.addU2AMapping("UDepartment", "dept");
		mySQLDB2.addTableColMapping("UDepartment", "dloc", "dlocation");
		unifiedRDBMS.put("jdbc:mysql://localhost:3306/mysql2", mySQLDB2);

		// ORDBMS
		// TableColMapping and U2AMapping for all cols bcz select * doesnt work, individual col names have to be mentioned
		ORDBMS.addU2AMapping("UEmployee", "Employee e");
		ORDBMS.addTableColMapping("UEmployee", "ecode", "e.ecode");
		ORDBMS.addTableColMapping("UEmployee", "ename", "e.ename");
		ORDBMS.addTableColMapping("UEmployee", "sal", "e.sal");
		ORDBMS.addTableColMapping("UEmployee", "year_bonus", "e.yearBonus"); // different than unifiedTableColumn
		ORDBMS.addTableColMapping("UEmployee", "did", "e.did");

		ORDBMS.addU2AMapping("UDepartment", "Department d");
		ORDBMS.addTableColMapping("UDepartment", "dcode", "d.dcode");
		ORDBMS.addTableColMapping("UDepartment", "dname", "d.dname");
		ORDBMS.addTableColMapping("UDepartment", "dloc", "d.dloc");
		ORDBMS.addTableColMapping("UDepartment", "phone_no", "d.phoneNo"); // different than unifiedTableColumn

		ORDBMS.addU2AMapping("UCountry", "Country c");
		ORDBMS.addTableColMapping("UCountry", "ccode", "c.ccode"); // this col is not in rdbms
		ORDBMS.addTableColMapping("UCountry", "cname", "c.cname");
		ORDBMS.addTableColMapping("UCountry", "capital", "c.capital");
		ORDBMS.addTableColMapping("UCountry", "population", "c.population");
		ORDBMS.addTableColMapping("UCountry", "continent", "c.continent");
	}

	private JFrame frame;
	private JTextField tfSelect;
	private JList<String> listFrom;
	private JTextField tfWhere;
	//private JTextField tfOther;

	// Initialize the contents of the frame
	public Main() {
		frame = new JFrame();
		frame.setVisible(true);
		frame.setTitle("Query Heterogenous Databases");
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JLabel lblSelect = new JLabel("SELECT");
		lblSelect.setBounds(12, 35, 70, 15);
		frame.getContentPane().add(lblSelect);

		JLabel lblFrom = new JLabel("FROM");
		lblFrom.setBounds(12, 61, 70, 15);
		frame.getContentPane().add(lblFrom);

		JLabel lblWhere = new JLabel("WHERE");
		lblWhere.setBounds(12, 115, 70, 15);
		frame.getContentPane().add(lblWhere);

		tfSelect = new JTextField();
		tfSelect.setColumns(10);
		tfSelect.setBounds(82, 35, 356, 25);
		frame.getContentPane().add(tfSelect);

		listFrom = new JList<>(unifiedTables);
		listFrom.setSelectionModel(new DefaultListSelectionModel() {
			private static final long serialVersionUID = 1L;

			// for multi select in list
			@Override
			public void setSelectionInterval(int index0, int index1) {
				if(super.isSelectedIndex(index0)) {
					super.removeSelectionInterval(index0, index1);
				}else {
					super.addSelectionInterval(index0, index1);
				}
			}
		});
		JScrollPane listScroller = new JScrollPane(listFrom);
		listScroller.setBounds(82, 61, 356, 50);
		frame.getContentPane().add(listScroller);

		tfWhere = new JTextField();
		tfWhere.setColumns(10);
		tfWhere.setBounds(82, 112, 356, 25);
		frame.getContentPane().add(tfWhere);

		/*
		tfOther = new JTextField();
		tfOther.setColumns(10);
		tfOther.setBounds(82, 138, 356, 25);
		frame.getContentPane().add(tfOther);
		 */

		JButton btnExecuteQuery = new JButton("Execute Query");
		btnExecuteQuery.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(listFrom.getSelectedValuesList().size() == 0) return;
				executeUnifiedQuery();
			}
		});
		btnExecuteQuery.setBounds(146, 233, 147, 25);
		frame.getContentPane().add(btnExecuteQuery);
	}

	private void executeUnifiedQuery() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
			return;
		}

		// get unified column names by executing query on each relational db once
		// and save the ResultSets for later use to fill unified table 
		HashSet<Object> unifiedColumnHeaders = new HashSet<>();
		HashMap<String, ResultSet> queryResultSets = new HashMap<>();
		ArrayList<Connection>  connections = new ArrayList<>(); // end all connection in the end
		for(String dbURL : unifiedRDBMS.keySet()) {
			try {
				Connection conn = DriverManager.getConnection(dbURL, mysqlDBUserName, mysqlDBPass);

				ArrayList<String> selectedTables = new ArrayList<>();
				HashSet<String> dbUnifiedColumns = new HashSet<>(); // all available unified cols in database
				Database db = unifiedRDBMS.get(dbURL);

				boolean dbContainsTables = true; // false if current db doesnt contain any of the selected tables
				for(String unifiedTableName : listFrom.getSelectedValuesList()) {
					String actualTableName = db.getActualTableName(unifiedTableName);
					if(actualTableName == null) {
						System.out.println(dbURL + " doesn't contain table: " + unifiedTableName);
						dbContainsTables = false;
						continue;
					}
					selectedTables.add(actualTableName);

					ResultSet cols = conn.getMetaData().getColumns(null, null, actualTableName, null);
					while(cols.next()) { // convert actual to unified col names
						String actualColName = cols.getString("COLUMN_NAME"); 
						dbUnifiedColumns.add(db.getUnifiedColName(actualColName));
					}
				}
				if(!dbContainsTables)	continue;

				// discard any cols in tfSelect if the db doesnt contain that col
				String tfSelectText = tfSelect.getText().replace(" ", "");
				if(tfSelectText.equals(""))	tfSelectText = "*";
				String querySelectText = "*";
				if(!tfSelectText.equals("*")) {
					ArrayList<String> selectedCols = new ArrayList<>(); // selected actual col names
					for(String c : tfSelectText.split(",")) {
						if(dbUnifiedColumns.contains(c))	selectedCols.add(db.getActualColName(c));
					}
					if(selectedCols.size() == 0) {
						System.out.println(dbURL + " doesn't contain any of the provided columns");
						continue;
					}
					querySelectText = String.join(",", selectedCols);
				}

				String query = "SELECT " + querySelectText + " FROM " + String.join(",", selectedTables);

				if(!tfWhere.getText().replace(" ", "").equals("")) { // replace unified col names by actual col names
					ArrayList<String> whereConditions = new ArrayList<>();
					for(String condition : tfWhere.getText().replace(" ","").split(",")) {
						String[] uCols = condition.split("=");
						String modifiedCondition = db.getActualColName(uCols[0]) + "=" + db.getActualColName(uCols[1]);
						whereConditions.add(modifiedCondition);
					}

					query += " WHERE " + String.join(",", whereConditions);
				}

				System.out.println(query);

				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query);
				queryResultSets.put(dbURL, rs);

				ResultSetMetaData metaData = rs.getMetaData();
				int noOfColumns = metaData.getColumnCount();
				for (int i = 0; i < noOfColumns; i++) {
					unifiedColumnHeaders.add(db.getUnifiedColName(metaData.getColumnName(i+1)));
				}

				connections.add(conn);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}

		// ordbms query creation
		String ordbmsSelectText = tfSelect.getText().replace(" ", "");
		ArrayList<String> ordbmsSelectedActualCols = new ArrayList<>();
		ArrayList<String> ordbmsSelectedUnifiedCols = new ArrayList<>();
		List<String> selectedUnifiedTables = listFrom.getSelectedValuesList();
		if(ordbmsSelectText.equals("") || ordbmsSelectText.equals("*")) { // get all cols for selected tables
			selectedUnifiedTables.forEach(unifiedTableName -> {
				TableDescription td = ORDBMS.getTable(unifiedTableName);
				td.getAllUnifiedColNames().forEach(unifiedColName -> {
					ordbmsSelectedActualCols.add(ORDBMS.getActualColName(unifiedColName));
					ordbmsSelectedUnifiedCols.add(unifiedColName);
				});
			});
		}else {// replace unified col names by actual and discard cols not present in db
			for(String unifiedColName : ordbmsSelectText.split(",")) {
				String actualColName = ORDBMS.getActualColName(unifiedColName);
				if(actualColName.equals(unifiedColName)) continue;
				ordbmsSelectedActualCols.add(actualColName);
				ordbmsSelectedUnifiedCols.add(unifiedColName);
			}
		}
		unifiedColumnHeaders.addAll(ordbmsSelectedUnifiedCols);

		List<String> selectedActualTables = new ArrayList<>();
		selectedUnifiedTables.forEach(unifiedTableName -> {
			selectedActualTables.add(ORDBMS.getActualTableName(unifiedTableName));
		});
		String ordbmsQuerySelectText = String.join(",", ordbmsSelectedActualCols);

		String ordbmsQuery = "SELECT " + ordbmsQuerySelectText + " FROM " + String.join(",", selectedActualTables);

		if(!tfWhere.getText().replace(" ", "").equals("")) { // replace unified col names by actual col names
			ArrayList<String> ordbmsWhereConditions = new ArrayList<>();
			for(String condition : tfWhere.getText().replace(" ","").split(",")) {
				String[] uCols = condition.split("=");
				String modifiedCondition = ORDBMS.getActualColName(uCols[0]) + "=" + ORDBMS.getActualColName(uCols[1]);
				ordbmsWhereConditions.add(modifiedCondition);
			}

			ordbmsQuery += " WHERE " + String.join(",", ordbmsWhereConditions);
		}

		System.out.println(ordbmsQuery);

		// assign each column with an index of the column in unified table
		int totalCols = unifiedColumnHeaders.size();
		Object[] columnHeaders = new Object[totalCols];
		HashMap<Object, Integer> unifiedColHeaderToIndex = new HashMap<>();
		int index = 0;
		for(Object header : unifiedColumnHeaders) {
			columnHeaders[index] = header;
			unifiedColHeaderToIndex.put(header, index);
			index++;
		}

		// Combined result
		ArrayList<Object[]> queryResult = new ArrayList<>();

		// ordbms query execution
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(ordbmsFile);
		EntityManager em = emf.createEntityManager();

		TypedQuery<Object[]> q = em.createQuery(ordbmsQuery, Object[].class);
		List<Object[]> results = q.getResultList();

		int noOfColumns = ordbmsSelectedUnifiedCols.size();
		for(Object[] r : results) {
			Object[] row = new Object[totalCols];
			for (int i = 0; i < noOfColumns; i++) {
				int colIndex = unifiedColHeaderToIndex.get(ordbmsSelectedUnifiedCols.get(i));
				row[colIndex] = r[i];
			}
			queryResult.add(row);
		}

		em.close();
		emf.close();

		// combine results
		for(String dbURL : unifiedRDBMS.keySet()) {
			Database db = unifiedRDBMS.get(dbURL);
			try {
				ResultSet rs = queryResultSets.get(dbURL);
				if(rs == null)	continue; // current db doesnt contain selected tables
				ResultSetMetaData metaData = rs.getMetaData();
				noOfColumns = metaData.getColumnCount();
				while (rs.next()) {
					Object[] row = new Object[totalCols];
					for (int i = 0; i < noOfColumns; i++) {
						int colIndex = unifiedColHeaderToIndex.get(db.getUnifiedColName(metaData.getColumnName(i+1)));
						row[colIndex] = rs.getObject(i+1);
					}
					queryResult.add(row);
				}

			} catch(SQLException e1) {
				e1.printStackTrace();
			}
		}

		// close all connections
		for(Connection conn : connections) {
			try {
				conn.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}

		// display result
		JTableDisplay(queryResult.toArray(new Object[queryResult.size()][]), columnHeaders);
	}

	// display result table on a new window
	public static void JTableDisplay(Object[][] rowData, Object[] columnName) {
		JFrame resultFrame = new JFrame("Unified Data");

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JTable table = new JTable(rowData, columnName);
		table.setEnabled(false);

		JScrollPane tableContainer = new JScrollPane(table);

		panel.add(tableContainer, BorderLayout.CENTER);
		resultFrame.getContentPane().add(panel);

		resultFrame.pack();
		resultFrame.setVisible(true);
	}

	// Launch the application
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new Main();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
