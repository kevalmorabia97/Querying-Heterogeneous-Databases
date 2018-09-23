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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Main {

	static List<String> dbURLs = Arrays.asList(
			"jdbc:mysql://localhost:3306/db_project",
			"jdbc:mysql://localhost:3306/db_project2",
			"jdbc:mysql://localhost:3306/db_project3"
			);
	static String dbUserName = "root";
	static String dbPass = "mysqlpass";

	private JFrame frame;
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frame.setVisible(true);
					window.frame.setTitle("Query Heterogenous Databases");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		textField = new JTextField();
		textField.setBounds(18, 101, 414, 25);
		frame.getContentPane().add(textField);
		textField.setColumns(10);

		JButton btnExecuteQuery = new JButton("Execute Query");
		btnExecuteQuery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnExecuteQuery.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				try {
					Class.forName("com.mysql.jdbc.Driver");
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}
				
				// get unified column names
				Set<Object> unifiedColumnHeaders = new HashSet<>();
				for(String dbURL : dbURLs) {
					try {
						Connection conn = DriverManager.getConnection(dbURL, dbUserName, dbPass);
						Statement stmt = conn.createStatement();
						ResultSet rs = stmt.executeQuery(textField.getText());
						ResultSetMetaData metaData = rs.getMetaData();
						int noOfColumns = metaData.getColumnCount();

						for(int i = 0; i < noOfColumns; i++) {
							unifiedColumnHeaders.add(metaData.getColumnName(i+1));
						}
						conn.close();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}

				int totalCols = unifiedColumnHeaders.size();
				Object[] columnHeaders = new Object[totalCols];
				HashMap<Object, Integer> colHeaderToIndex = new HashMap<>();
				int index = 0;
				for(Object header : unifiedColumnHeaders) {
					columnHeaders[index] = header;
					colHeaderToIndex.put(header, index);
					index++;
				}
				
				ArrayList<Object[]> queryResult = new ArrayList<>();
				for(String dbURL : dbURLs) {
					try {
						Connection conn = DriverManager.getConnection(dbURL, dbUserName, dbPass);

						Statement stmt = conn.createStatement();
						ResultSet rs = stmt.executeQuery(textField.getText());
						ResultSetMetaData metaData = rs.getMetaData();
						int noOfColumns = metaData.getColumnCount();

						while (rs.next()) {
							Object[] row = new Object[totalCols];
							for (int i = 0; i < noOfColumns; i++) {
								int colIndex = colHeaderToIndex.get(metaData.getColumnName(i+1));
								row[colIndex] = rs.getObject(i+1);
							}
							queryResult.add(row);
						}

						conn.close();
					} catch(SQLException e1) {
						e1.printStackTrace();
					}
				}

				JTableDisplay(queryResult.toArray(new Object[queryResult.size()][]), columnHeaders);
			}

			public void JTableDisplay(Object[][] rowData, Object[] columnName) {
				JFrame frame = new JFrame("JTable Test Display");

				JPanel panel = new JPanel();
				panel.setLayout(new BorderLayout());

				JTable table = new JTable(rowData, columnName);
				table.setEnabled(false);

				JScrollPane tableContainer = new JScrollPane(table);

				panel.add(tableContainer, BorderLayout.CENTER);
				frame.getContentPane().add(panel);

				frame.pack();
				frame.setVisible(true);
			}

		});
		btnExecuteQuery.setBounds(146, 138, 147, 25);
		frame.getContentPane().add(btnExecuteQuery);
	}
}
