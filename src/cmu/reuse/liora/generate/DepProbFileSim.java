package cmu.reuse.liora.generate;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DepProbFileSim implements Simulator {

	@Override
	public void simulate(Menu menu, Column column, List<Individual> people, List<Column> allColumns) {
		System.out.println("Please enter the datatype on which file choice depends");
		Column depColumn = menu.getColumn(allColumns);
		Map<String, File> valueToFile = menu.getFileDeps();
		Map<File, CSVReader> fileToReader = new HashMap<>();
		for (File f : valueToFile.values()) {
			CSVReader reader = new CSVReader(f);
			System.out.println("For file " + f.getName() + " please enter the following");
			System.out.println("Please enter the name of the column with the probabilities");
			Column probColumn = menu.getColumn(reader.header);
			System.out.println("Please enter the name of the column with the possible values.");	
			Column valColumn = menu.getColumn(reader.header);
			int format = menu.getDataFormat(); //1 is percentage 2 is freq
			if (format == 1) {
				reader.parseProbs(valColumn, probColumn);
			} else {
				reader.parseFreqs(valColumn, probColumn);
			}
			fileToReader.put(f, reader);			
		}		
		for (Individual person : people) {
			String currValue = person.getValues().get(depColumn);
			File file = valueToFile.get(currValue); 
			CSVReader reader = fileToReader.get(file); 
			person.setValue(column, reader.calculate());
		}
		for (CSVReader r : fileToReader.values()) {
			r.close();
		}

	}

}
