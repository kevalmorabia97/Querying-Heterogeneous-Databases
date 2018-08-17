import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Main {

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
					Connection con = DriverManager.getConnection(
							"jdbc:mysql://localhost:3306/db_project","root","mysqlpass");

					Statement stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery(textField.getText());
					ResultSetMetaData metaData = rs.getMetaData();
					int noOfColumns = metaData.getColumnCount();
					
					Object[] columnHeaders = new Object[noOfColumns];
					for(int i = 0; i < noOfColumns; i++)
						columnHeaders[i] = metaData.getColumnName(i+1);

					ArrayList<Object[]> queryResult = new ArrayList<>();	
					while (rs.next()) {
						Object[] row = new Object[noOfColumns];
						for (int i = 0; i < noOfColumns; i++) {
							row[i] = rs.getObject(i+1);
						}
						queryResult.add(row);
					}
					
					JTableDisplay(queryResult.toArray(new Object[queryResult.size()][]), columnHeaders);

					con.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
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
