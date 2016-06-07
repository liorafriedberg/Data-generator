package cmu.reuse.liora.generate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Column {
	Source source; 
	String datatype; //column name
	String label;
	boolean percentages; //can use
	List<Column> dependencies; //can use
	String[] values; //can use
	File file; //which file is in
	
	public Column() {
		dependencies = new ArrayList<>();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		Column input = (Column) obj;
		return (input.datatype.equals(datatype) && input.file.equals(file));
	}
 }
