package cmu.reuse.liora.generate;

import java.io.File;
import java.util.List;
import java.util.Map;

public class DepStaticSim implements Simulator {

	@Override
	public void simulate(Menu menu, Column column, List<Individual> people, List<Column> cols) {
		File file = menu.getFile();
		CSVReader reader = new CSVReader(file);
		System.out.println("For datatype " + column.datatype + ", please enter the datatype"
				+ " on which its generation depends.");
		Column dependency = menu.getColumn(reader.header);			
		int columnIndex = reader.getColumnIndex(column); //index of column value want
		int depIndex = reader.getColumnIndex(dependency);			
		Map<String, String> values = reader.findStaticValue(depIndex, columnIndex); 
		reader.close();
		for (Individual person : people) {	
		Map<Column, String> currentValues = person.getValues();
		String curr = currentValues.get(dependency); 		
		String value = values.get(curr);
		person.setValue(column, value);
		}	
	}

}
