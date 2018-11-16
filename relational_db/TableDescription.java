package relational_db;
import java.util.HashMap;

public class TableDescription {
	private String actualName;
	private HashMap<String, String> unified2ActualColMap;
	private HashMap<String, String> actual2UnifiedColMap;
	
	public TableDescription(String actualName) {
		this.actualName = actualName;
		unified2ActualColMap = new HashMap<>();
		actual2UnifiedColMap = new HashMap<>();
	}
	
	public void addU2AColName(String unifiedCol, String actualCol) {
		unified2ActualColMap.put(unifiedCol, actualCol);
		actual2UnifiedColMap.put(actualCol, unifiedCol);
	}
	
	public String getActualName() {
		return actualName;
	}
	
	public String getU2AColName(String unifiedCol) {
		return unified2ActualColMap.getOrDefault(unifiedCol, unifiedCol);
	}
	
	public String getA2UColName(String actualCol) {
		return unified2ActualColMap.getOrDefault(actualCol, actualCol);
	}
}
