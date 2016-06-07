package cmu.reuse.liora.generate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Individual {
	
	private Map<Column, String> values;
	
	Individual() {
		values = new HashMap<>();
	}
	
	public void setValue(Column c, String v) {
		values.put(c, v);
	}
	
	public Map<Column, String> getValues() {
		return values;
	}

}
