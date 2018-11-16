package relational_db;
import java.util.HashMap;

public class RelationalDatabase {
	private HashMap<String, TableDescription> unified2ActualTable;
	private HashMap<String, String> dbU2AColNames;
	private HashMap<String, String> dbA2UColNames;
	
	public RelationalDatabase() {
		unified2ActualTable = new HashMap<>();
		dbU2AColNames = new HashMap<>();
		dbA2UColNames = new HashMap<>();
	}
	
	public void addU2AMapping(String unifiedTableName, String actualTableName) {
		unified2ActualTable.put(unifiedTableName, new TableDescription(actualTableName));
	}
	
	public TableDescription getTable(String unifiedTableName) {
		return unified2ActualTable.get(unifiedTableName);
	}
	
	public void addTableColMapping(String unifiedTableName, String unifiedColName, String actualColName) {
		getTable(unifiedTableName).addU2AColName(unifiedColName, actualColName);
		dbU2AColNames.put(unifiedColName, actualColName);
		dbA2UColNames.put(actualColName, unifiedColName);
	}
	
	public boolean containsTable(String unifiedTableName) {
		return unified2ActualTable.containsKey(unifiedTableName);
	}
	
	public String getActualTableName(String unifiedTableName) {
		return containsTable(unifiedTableName) ? getTable(unifiedTableName).getActualName() : null;
	}
	
	public String getActualColName(String unifiedColName) {
		return dbU2AColNames.getOrDefault(unifiedColName, unifiedColName);
	}
	
	public String getUnifiedColName(String actualColName) {
		return dbA2UColNames.getOrDefault(actualColName, actualColName);
	}
}
