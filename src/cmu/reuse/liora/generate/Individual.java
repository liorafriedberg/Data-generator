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
	
	private Map<Column, String> values;
	
	Individual() {
		values = new HashMap<>();
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
