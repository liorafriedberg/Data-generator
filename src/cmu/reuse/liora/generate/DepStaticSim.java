package cmu.reuse.liora.generate;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * generates individual-level data from static values that depend on previous values
 * @author liorafriedberg
 */
public class DepStaticSim implements Simulator {
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Simulator#simulate(cmu.reuse.liora.generate.Menu, cmu.reuse.liora.generate.Column, java.util.List, java.util.List)
	 */
	@Override
	public void simulate(Menu menu, Column column, List<Individual> people, List<Column> cols) {
		File file = menu.getFile(column);
		CSVReader reader = new CSVReader(file);		
		List<Column> dependencies = menu.getDependencies(column, reader.header);
		System.out.println("Please enter the name of the column with the possible values.");	
		Column valColumn = menu.getValueColumn(reader.header, column);
		int columnIndex = reader.getColumnIndex(valColumn); //index of column value want
		Map<Integer, Column> indexToDep = new HashMap<>();
		Set<Column> check = people.get(0).getValues().keySet();
		for (Column d : dependencies) {
		int depIndex = reader.getColumnIndex(d);	
		indexToDep.put(depIndex, d);
		if (!check.contains(d)) {
			throw new IllegalArgumentException("Chose a dependency not yet assigned");
		}	
	}
		Map<Map<Column, String>, String> values = reader.findStaticValue(indexToDep, columnIndex); 

		reader.close();
		for (Individual person : people) {	
		Map<Column, String> currentValues = person.getValues();
		Map<Column, String> currentDepVals = new HashMap<>();
		for (Column d : dependencies) {		
			currentDepVals.put(d, currentValues.get(d));
		}		
		String value = values.get(currentDepVals);
		person.setValue(column, value);
		}	
	}

}
