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

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import relational_db.RelationalDatabase;

public class Main {

	static String mysqlDBUserName = "root";
	static String mysqlDBPass = "root";

	/*
	 *  key: url of database
	 *  value: map from unified table name to actual table name
	 */
	static HashMap<String, RelationalDatabase> unifiedDB = new HashMap<>();
	static String[] unifiedTables = new String[] {"UEmployee", "UDepartment", "UCountry"};
	static {
		// DATABASE 1		
		RelationalDatabase mySQLDB1 = new RelationalDatabase();
		mySQLDB1.addU2AMapping("UEmployee", "employee");
		mySQLDB1.addTableColMapping("UEmployee", "ecode", "eid"); // ecode in UEmployee = eid in employee of mysql1
		mySQLDB1.addTableColMapping("UEmployee", "sal", "wage");
		mySQLDB1.addU2AMapping("UDepartment", "department");
		mySQLDB1.addU2AMapping("UCountry", "country");
		unifiedDB.put("jdbc:mysql://localhost:3306/mysql1", mySQLDB1);

		// DATABASE 2		
		RelationalDatabase mySQLDB2 = new RelationalDatabase();
		mySQLDB2.addU2AMapping("UEmployee", "empl");
		mySQLDB2.addU2AMapping("UDepartment", "dept");
		mySQLDB2.addTableColMapping("UDepartment", "dloc", "dlocation");
		unifiedDB.put("jdbc:mysql://localhost:3306/mysql2", mySQLDB2);
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
				}
				else {
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

				try {
					Class.forName("com.mysql.jdbc.Driver");
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}

				// get unified column names by executing query on each db once
				// and save the ResultSets for later use to fill unified table 
				HashSet<Object> unifiedColumnHeaders = new HashSet<>();
				HashMap<String, ResultSet> queryResultSets = new HashMap<>();
				ArrayList<Connection>  connections = new ArrayList<>(); // end all connection in the end
				for(String dbURL : unifiedDB.keySet()) {
					try {
						Connection conn = DriverManager.getConnection(dbURL, mysqlDBUserName, mysqlDBPass);

						ArrayList<String> selectedTables = new ArrayList<>();
						HashSet<String> dbUnifiedColumns = new HashSet<>(); // all available unified cols in database
						RelationalDatabase db = unifiedDB.get(dbURL);

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
						
						if(!tfWhere.getText().equals("")) { // replace unified col names by actual col names
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

				// combine results
				ArrayList<Object[]> queryResult = new ArrayList<>();
				for(String dbURL : unifiedDB.keySet()) {
					RelationalDatabase db = unifiedDB.get(dbURL);
					try {
						ResultSet rs = queryResultSets.get(dbURL);
						if(rs == null)	continue; // current db doesnt contain selected tables
						ResultSetMetaData metaData = rs.getMetaData();
						int noOfColumns = metaData.getColumnCount();
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
		});
		btnExecuteQuery.setBounds(146, 233, 147, 25);
		frame.getContentPane().add(btnExecuteQuery);
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
		//new Main();
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
