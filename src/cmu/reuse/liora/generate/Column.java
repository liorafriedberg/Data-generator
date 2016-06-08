package cmu.reuse.liora.generate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * represents a column in the database, a datatype
 * @author liorafriedberg
 */
public class Column { 
	/**
	 * column name
	 */
	String datatype;
	String label;
	
	public Column() {
	}
	
	public Column(String datatype) {
		this.datatype = datatype;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		Column input = (Column) obj;
		return (input.datatype.equals(datatype));
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return datatype.hashCode(); 
	}
 }
