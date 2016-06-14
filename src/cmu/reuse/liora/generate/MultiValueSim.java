package cmu.reuse.liora.generate;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiValueSim implements Simulator {

	@Override
	public void simulate(Menu menu, Column column, List<Individual> people, List<Column> allColumns) {
		File file = menu.getFile();
		CSVReader reader = new CSVReader(file);
		List<Column> dependencies = menu.getDependencies(column, reader.header);
		List<Column> potentialValues = menu.getPotentialValues(column, reader.header);	
		menu.getLabels(potentialValues); //these columns now have the label variable
		Map<Integer, Column> indexToPotential = new HashMap<>();
		for (Column col : potentialValues) {
			int index = reader.getColumnIndex(col);
			indexToPotential.put(index, col);
		} //index to potential has the potential values mapped with their indices
		reader.close();
		int format = menu.getDataFormat();
			
		for (Individual person : people) {
			CSVReader pReader = new CSVReader(file);
			Map<Integer, String> indexToValue = new HashMap<>(); //index of dep cols and what vals looking for
			Map<Column, String> currentValues = person.getValues();
			for (Column d : dependencies) {
				int index = pReader.getColumnIndex(d);
				indexToValue.put(index, currentValues.get(d));
			}
			if (format == 1) {
				pReader.parseProbsDep(indexToValue, indexToPotential);
			} 
			else { //== 2
				pReader.parseFreqsDep(indexToValue, indexToPotential);
			}									
			person.setValue(column, pReader.multiCalculate()); //this one line is only difference
			//can extract this
			pReader.close();
		}
	
	}

}
