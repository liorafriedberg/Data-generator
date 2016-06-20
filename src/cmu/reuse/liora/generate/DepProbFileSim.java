package cmu.reuse.liora.generate;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * generates individual-level data from a file (with a probability distribution) that is dependent on 
 * previous values
 * @author liorafriedberg
 */
public class DepProbFileSim implements Simulator {

	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Simulator#simulate(cmu.reuse.liora.generate.Menu, cmu.reuse.liora.generate.Column, java.util.List, java.util.List)
	 */
	@Override
	public void simulate(Menu menu, Column column, List<Individual> people, List<Column> allColumns) {
		System.out.println("Please enter the datatype on which file choice depends");
		Column depColumn = menu.getDepColumn(allColumns, column);
		 Set<Column> check = people.get(0).getValues().keySet();
		 if (!check.contains(depColumn)) {
				throw new IllegalArgumentException("Chose a dependency not yet assigned");
			}
		Map<String, File> valueToFile = menu.getFileDeps(column);
		Map<File, CSVReader> fileToReader = new HashMap<>();
		for (File f : valueToFile.values()) {
			CSVReader reader = new CSVReader(f);
			System.out.println("For file " + f.getName() + " please enter the following");
			System.out.println("Please enter the name of the column with the probabilities");
			Column probColumn = menu.getProbColumn(reader.header, column);
			System.out.println("Please enter the name of the column with the possible values.");	
			Column valColumn = menu.getValueColumn(reader.header, column);
			int format = menu.getDataFormat(column); //1 is percentage 2 is freq
			if (format == 1) {
				reader.parseProbs(valColumn, probColumn);
			} else {
				reader.parseFreqs(valColumn, probColumn);
			}
			reader.binaryHelper();
			fileToReader.put(f, reader);			
		}		
		for (Individual person : people) {
			String currValue = person.getValues().get(depColumn);
			File file = valueToFile.get(currValue); 
			CSVReader reader = fileToReader.get(file); 
		
			person.setValue(column, reader.calculateBinary());
		}
		for (CSVReader r : fileToReader.values()) {
			r.close();
		}

	}

}
