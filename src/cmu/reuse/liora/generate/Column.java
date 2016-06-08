package cmu.reuse.liora.generate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liorafriedberg
 * represents a column in the database, a datatype
 */
public class Column { 
	/**
	 * column name
	 */
	String datatype;
	String label;
	
	public Column() {
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 * check if same column name for equality
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		Column input = (Column) obj;
		return (input.datatype.equals(datatype));
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 * use column name hashcode
	 */
	@Override
	public int hashCode() {
		return datatype.hashCode(); 
	}
 }
