package cmu.reuse.liora.generate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DepProbSim implements Simulator {
	 
	@Override
	public void simulate(Menu menu, Column column, List<Individual> people, List<Column> cols) {
		File file = menu.getFile(column);								
		CSVReader reader = new CSVReader(file);
		List<Column> dependencies = menu.getDependencies(column, reader.header);		
		List<Column> potentialValues = menu.getPotentialValues(column, reader.header);				
		menu.getLabels(potentialValues); //these columns now have the label variable	

		Map<Integer, Column> indexToP = new HashMap<>();
		Map<Integer, Column> indexToDep = new HashMap<>();
		for (Column col : potentialValues) {
			int index = reader.getColumnIndex(col);
			indexToP.put(index, col);
		} //index to potential has the potential values mapped with their indices
		Set<Column> check = people.get(0).getValues().keySet();
		for (Column d : dependencies) {
			int index = reader.getColumnIndex(d);
			indexToDep.put(index, d);
			if (!check.contains(d)) {
				throw new IllegalArgumentException("Chose a dependency not yet assigned");
			} 	
		}	
		Map<Map<Column, String>, Map<Column, String>> save = reader.findDepValues(indexToDep, indexToP);
		reader.close();
		int format = menu.getDataFormat(column);		
		
		for (Individual person : people) {
			Map<Column, String> currentValues = person.getValues();
			Map<Column, String> currentDepVals = new HashMap<>();
			for (Column d : dependencies) {
				currentDepVals.put(d, currentValues.get(d));
			}		
			Map<Column, String> pVals = save.get(currentDepVals);
			CSVReader personReader = new CSVReader(file);
								
			if (format == 1) {
				personReader.parseProbsDep(pVals);
			} 
			else { //== 2
				personReader.parseFreqsDep(pVals);
			}														
			person.setValue(column, personReader.calculate());
			personReader.close();
		}

	}

}
