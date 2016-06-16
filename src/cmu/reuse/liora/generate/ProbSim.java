package cmu.reuse.liora.generate;

import java.io.File;
import java.util.List;

public class ProbSim implements Simulator {

	@Override
	public void simulate(Menu menu, Column column, List<Individual> people, List<Column> cols) {
		File file = menu.getFile(column);
		CSVReader reader = new CSVReader(file);
		System.out.println("Please enter the name of the column with the probabilities.");
		Column probColumn = menu.getProbColumn(reader.header, column);
		System.out.println("Please enter the name of the column with the possible values.");	
		Column valColumn = menu.getValueColumn(reader.header, column);
		//value column is column, probColumn is frequency column
		int format = menu.getDataFormat(column); //1 is percentage 2 is freq
		
		if (format == 1) {
			reader.parseProbs(valColumn, probColumn);
		}
		else { //== 2
			reader.parseFreqs(valColumn, probColumn);
		}				
		for (Individual person : people) {
			person.setValue(column, reader.calculate());
		}
		reader.close();
	}

}
