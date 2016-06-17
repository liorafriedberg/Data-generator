package cmu.reuse.liora.generate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * represents a person with specific values for the datatypes, corresponds to a row in the database
 * @author liorafriedberg
 */
public class Individual {
	
	/**
	 * column to value for columns that strictly have one value (or a concatenated string)
	 */
	private Map<Column, String> values;
	
	/**
	 * for multi-value columns beyond depth 1 - map from one value to a corresponding value
	 * e.g. disease(a) to prescription(1), disease(a) to prescription(2), disease(b) to ...
	 */
	Map<String, String> mvTwo;
	
	Individual() {
		values = new HashMap<>();
		mvTwo = new HashMap<>();
	}
	
	/**
	 * @param c		column to set
	 * @param v		value to set
	 */
	public void setValue(Column c, String v) {
		values.put(c, v);
	}
	
	/**
	 * @return		columns to values for the columns
	 */
	public Map<Column, String> getValues() {
		return values;
	}

}
