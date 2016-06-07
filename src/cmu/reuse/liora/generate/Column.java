package cmu.reuse.liora.generate;

import java.util.ArrayList;
import java.util.List;

public class Column {
	Source source; 
	String datatype; //column name
	String label;
	boolean percentages; //can use
	List<Column> dependencies; //can use
	String[] values;
	
	public Column() {
		dependencies = new ArrayList<>();
	}
 }
